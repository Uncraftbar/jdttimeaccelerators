package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.direwolf20.justdirethings.common.items.TimeWand;
import com.direwolf20.justdirethings.util.MiscTools;
import com.uncraftbar.jdttimeaccelerators.common.acceleration.AccelerationCoordinator;
import com.uncraftbar.jdttimeaccelerators.config.JDTTAConfig;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.stacks.AEFluidKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Performs one atomic-ish network-funded acceleration operation per grid tick. */
public final class AE2AccelerationEngine {
    private static final int WAND_DURATION = 600;
    /** Stale-target timeout. Observed progress refreshes it, so this is not a duration lock. */
    private static final int REQUEST_STALE_TIMEOUT = 600;
    /** Brief tolerance for machines whose persisted state changes only every few ticks. */
    private static final int REQUEST_IDLE_GRACE_TICKS = 3;
    private AE2AccelerationEngine() {}

    public static boolean isCardInstalled(AE2AccelerationHost host) {
        // AE2 may ask a newly-created ticker for its ticking request while the grid is being
        // readied, before our constructor-tail injection has created the optional inventory.
        // Treat that tiny initialization window as "no card" instead of crashing the server.
        var card = Registration.AE2_TIME_ACCELERATION_CARD.get();
        var accelerationUpgrades = host.jdtta$getAccelerationUpgrades();
        if (accelerationUpgrades != null && accelerationUpgrades.isInstalled(card)) return true;

        // AppliedFlux adds its own upgrade inventory to Pattern Providers. Keep our extra
        // slot so an Induction Card and Time Acceleration Card can coexist, but recognize
        // our card in AppliedFlux's original slot too. AdvancedAE exposes that inventory
        // through the same AE2 interface, including the Small Advanced provider.
        var hostUpgrades = host.jdtta$getExternalUpgrades();
        return hostUpgrades != null
                && hostUpgrades != accelerationUpgrades
                && hostUpgrades.isInstalled(card);
    }

    /** Wake AE2's normal provider ticker after a card is inserted into an upgrade
     * inventory owned by another mod. Those inventories do not know about our ticker. */
    public static void alertTicker(AE2AccelerationHost host) {
        var node = host.jdtta$getMainNode();
        if (node != null) {
            node.ifPresent((grid, gridNode) -> grid.getTickManager().alertDevice(gridNode));
        }
    }

    public static int requestedStaleTimeout() { return REQUEST_STALE_TIMEOUT; }

    public static int maxSpeedLevel() {
        int max = Math.max(2, JDTTAConfig.maxAccelerationMultiplier());
        int level = 1;
        while (level < 30 && (1 << level) < max) level++;
        if ((1 << Math.min(level, 30)) > max) level--;
        return Math.max(1, level);
    }

    public static boolean shouldTickUrgently(AE2AccelerationHost host) {
        if (!isCardInstalled(host)) return false;
        if (host.jdtta$isPatternProvider() && host.jdtta$isConditional()) {
            return host.jdtta$getRequestedTarget() != null;
        }
        if (!host.jdtta$isPatternProvider() && host.jdtta$isConditional()) {
            BlockEntity be = host.jdtta$getHostBlockEntity();
            return be.getLevel() != null && be.getLevel().hasNeighborSignal(be.getBlockPos());
        }
        return true;
    }

    public static void tick(AE2AccelerationHost host) {
        if (!isCardInstalled(host)) return;
        var node = host.jdtta$getMainNode();
        if (!node.isActive() || node.getGrid() == null) return;
        var be = host.jdtta$getHostBlockEntity();
        if (!(be.getLevel() instanceof ServerLevel level)) return;

        if (host.jdtta$isConditional() && host.jdtta$isPatternProvider()) {
            BlockPos requested = host.jdtta$getRequestedTarget();
            // A successful push identifies the exact target. This is adaptive rather than a
            // fixed 30-second effect: it stops within a few ticks of completion, while observed
            // progress keeps refreshing the stale-target timeout for genuinely long jobs.
            if (requested == null || level.getGameTime() > host.jdtta$getRequestedTargetExpiry()) {
                clearRequestedTarget(host);
                return;
            }
            BlockPos hostPos = be.getBlockPos();
            Direction requestedSide = Direction.fromDelta(
                    requested.getX() - hostPos.getX(),
                    requested.getY() - hostPos.getY(),
                    requested.getZ() - hostPos.getZ());
            if (requestedSide == null
                    || !host.jdtta$getSelectedTargetDirections().contains(requestedSide)) {
                clearRequestedTarget(host);
                return;
            }
            accelerateRequested(host, level, requested);
            return;
        }

        // Interfaces use deterministic redstone control instead of attempting to
        // infer a third-party machine's internal working state.
        if (host.jdtta$isConditional() && !level.hasNeighborSignal(be.getBlockPos())) return;

        // Every selected eligible target gets its own funded operation. This lets
        // one provider accelerate parallel machines while preserving exact costs:
        // if resources run out, only the targets already paid for are accelerated.
        for (Direction direction : host.jdtta$getSelectedTargetDirections()) {
            BlockPos target = be.getBlockPos().relative(direction);
            accelerate(host, level, target);
        }
    }

    private static void accelerateRequested(AE2AccelerationHost host, ServerLevel level, BlockPos targetPos) {
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target == null) {
            clearRequestedTarget(host);
            return;
        }

        long before = machineFingerprint(target, level);
        AccelerationResult result = accelerate(host, level, targetPos);
        if (result == AccelerationResult.ALREADY_ACCELERATED) {
            // Another Interface/Pattern Provider won this server tick. Keep the crafting
            // request alive so this host can try again instead of forgetting an active job.
            return;
        }
        if (result != AccelerationResult.ACCELERATED) {
            clearRequestedTarget(host);
            return;
        }
        long after = machineFingerprint(target, level);

        boolean observedWork = before != after;
        int idleTicks = observedWork ? 0 : host.jdtta$getRequestedTargetIdleTicks() + 1;
        host.jdtta$setRequestedTargetIdleTicks(idleTicks);

        if (idleTicks >= REQUEST_IDLE_GRACE_TICKS) {
            clearRequestedTarget(host);
        } else if (observedWork) {
            // Keep long-running crafts alive without imposing a fixed acceleration duration.
            host.jdtta$setRequestedTarget(targetPos, level.getGameTime() + REQUEST_STALE_TIMEOUT);
        }
    }

    private static long machineFingerprint(BlockEntity target, ServerLevel level) {
        try {
            return target.saveWithoutMetadata(level.registryAccess()).hashCode();
        } catch (RuntimeException ignored) {
            // A broken third-party serializer must not crash AE2's grid ticker. Such machines
            // receive only the short idle-grace burst unless they expose a working-state method.
            return Long.MIN_VALUE;
        }
    }

    private static void clearRequestedTarget(AE2AccelerationHost host) {
        if (host.jdtta$getRequestedTarget() != null) {
            host.jdtta$setRequestedTarget(null, 0);
            host.jdtta$setRequestedTargetIdleTicks(0);
            host.jdtta$saveChanges();
        }
    }

    private static AccelerationResult accelerate(AE2AccelerationHost host, ServerLevel level, BlockPos targetPos) {
        if (targetPos.equals(host.jdtta$getHostBlockEntity().getBlockPos())) return AccelerationResult.FAILED;
        var state = level.getBlockState(targetPos);
        var target = level.getBlockEntity(targetPos);
        if (!MiscTools.isValidTickAccelBlock(level, state, target)) return AccelerationResult.FAILED;

        int speedLevel = Math.max(1, Math.min(host.jdtta$getSpeedLevel(), maxSpeedLevel()));
        int rate = Math.min((int) TimeWandEntity.calculateAccelRate(speedLevel),
                JDTTAConfig.maxAccelerationMultiplier());
        int feCost = Math.multiplyExact(rate, TimeWand.getFEPerRate());
        int fullFluidCost = cumulativeFluidCost(rate);
        int fluidCost = (host.jdtta$getFluidRemainder() + fullFluidCost) / WAND_DURATION;

        var grid = host.jdtta$getMainNode().getGrid();
        if (grid == null) return AccelerationResult.FAILED;
        var fluidKey = AEFluidKey.of(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get());
        var storage = grid.getStorageService().getInventory();
        var source = host.jdtta$getActionSource();
        double aeCost = PowerUnit.FE.convertTo(PowerUnit.AE, feCost);

        if (fluidCost > 0 && storage.extract(fluidKey, fluidCost, Actionable.SIMULATE, source) != fluidCost) {
            return AccelerationResult.FAILED;
        }
        if (grid.getEnergyService().extractAEPower(aeCost, Actionable.SIMULATE, PowerMultiplier.ONE) + 1.0e-7 < aeCost) {
            return AccelerationResult.FAILED;
        }

        // Claim only after resource simulation, but before charging the network. This makes
        // competing hosts free to try if this one cannot pay while ensuring only the winner
        // consumes resources and performs extra ticks.
        if (!AccelerationCoordinator.tryClaim(level, targetPos)) {
            return AccelerationResult.ALREADY_ACCELERATED;
        }

        long extractedFluid = fluidCost <= 0 ? 0 : storage.extract(fluidKey, fluidCost, Actionable.MODULATE, source);
        if (extractedFluid != fluidCost) {
            if (extractedFluid > 0) storage.insert(fluidKey, extractedFluid, Actionable.MODULATE, source);
            AccelerationCoordinator.release(level, targetPos);
            return AccelerationResult.FAILED;
        }
        double extractedPower = grid.getEnergyService().extractAEPower(aeCost, Actionable.MODULATE, PowerMultiplier.ONE);
        if (extractedPower + 1.0e-7 < aeCost) {
            if (extractedFluid > 0) storage.insert(fluidKey, extractedFluid, Actionable.MODULATE, source);
            if (extractedPower > 0) grid.getEnergyService().injectPower(extractedPower, Actionable.MODULATE);
            AccelerationCoordinator.release(level, targetPos);
            return AccelerationResult.FAILED;
        }

        host.jdtta$setFluidRemainder((host.jdtta$getFluidRemainder() + fullFluidCost) % WAND_DURATION);
        MiscTools.doExtraTicks(level, targetPos, rate);
        return AccelerationResult.ACCELERATED;
    }

    private static int cumulativeFluidCost(int rate) {
        int result = 0;
        for (int wandRate = 2; wandRate <= rate; wandRate *= 2) {
            result += (int) (wandRate * TimeWand.getMBPerRate());
            if (wandRate > Integer.MAX_VALUE / 2) break;
        }
        return result;
    }

    private enum AccelerationResult {
        ACCELERATED,
        ALREADY_ACCELERATED,
        FAILED
    }

}
