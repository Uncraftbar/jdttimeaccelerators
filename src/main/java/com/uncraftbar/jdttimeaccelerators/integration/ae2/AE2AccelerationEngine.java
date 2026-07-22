package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.direwolf20.justdirethings.common.items.TimeWand;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MiscTools;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.blockentities.ICraftingMachine;
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
    private static final Map<Class<?>, @Nullable Method> WORK_METHODS = new ConcurrentHashMap<>();

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
        int max = Math.max(2, Config.TIME_WAND_MAX_MULTIPLIER.get());
        int level = 1;
        while (level < 30 && (1 << level) < max) level++;
        if ((1 << Math.min(level, 30)) > max) level--;
        return Math.max(1, level);
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
            accelerateRequested(host, level, requested);
            return;
        }

        for (Direction direction : host.jdtta$getTargetDirections()) {
            BlockPos target = be.getBlockPos().relative(direction);
            if (host.jdtta$isConditional() && !isMachineWorking(level, target)) continue;
            if (accelerate(host, level, target)) return; // one adjacent target per card/tick
        }
    }

    private static void accelerateRequested(AE2AccelerationHost host, ServerLevel level, BlockPos targetPos) {
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target == null) {
            clearRequestedTarget(host);
            return;
        }

        long before = machineFingerprint(target, level);
        Boolean workingBefore = getMachineWorkingState(target);
        if (!accelerate(host, level, targetPos)) {
            clearRequestedTarget(host);
            return;
        }
        long after = machineFingerprint(target, level);
        Boolean workingAfter = getMachineWorkingState(target);

        boolean observedWork = Boolean.TRUE.equals(workingBefore)
                || Boolean.TRUE.equals(workingAfter)
                || before != after;
        int idleTicks = observedWork ? 0 : host.jdtta$getRequestedTargetIdleTicks() + 1;
        host.jdtta$setRequestedTargetIdleTicks(idleTicks);

        if (Boolean.FALSE.equals(workingAfter) || idleTicks >= REQUEST_IDLE_GRACE_TICKS) {
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

    private static boolean accelerate(AE2AccelerationHost host, ServerLevel level, BlockPos targetPos) {
        if (targetPos.equals(host.jdtta$getHostBlockEntity().getBlockPos())) return false;
        var state = level.getBlockState(targetPos);
        var target = level.getBlockEntity(targetPos);
        if (!MiscTools.isValidTickAccelBlock(level, state, target)) return false;

        int speedLevel = Math.max(1, Math.min(host.jdtta$getSpeedLevel(), maxSpeedLevel()));
        int rate = Math.min((int) TimeWandEntity.calculateAccelRate(speedLevel), Config.TIME_WAND_MAX_MULTIPLIER.get());
        int feCost = Math.multiplyExact(rate, TimeWand.getFEPerRate());
        int fullFluidCost = cumulativeFluidCost(rate);
        int fluidCost = (host.jdtta$getFluidRemainder() + fullFluidCost) / WAND_DURATION;

        var grid = host.jdtta$getMainNode().getGrid();
        if (grid == null) return false;
        var fluidKey = AEFluidKey.of(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get());
        var storage = grid.getStorageService().getInventory();
        var source = host.jdtta$getActionSource();
        double aeCost = PowerUnit.FE.convertTo(PowerUnit.AE, feCost);

        if (fluidCost > 0 && storage.extract(fluidKey, fluidCost, Actionable.SIMULATE, source) != fluidCost) return false;
        if (grid.getEnergyService().extractAEPower(aeCost, Actionable.SIMULATE, PowerMultiplier.ONE) + 1.0e-7 < aeCost) return false;

        long extractedFluid = fluidCost <= 0 ? 0 : storage.extract(fluidKey, fluidCost, Actionable.MODULATE, source);
        if (extractedFluid != fluidCost) {
            if (extractedFluid > 0) storage.insert(fluidKey, extractedFluid, Actionable.MODULATE, source);
            return false;
        }
        double extractedPower = grid.getEnergyService().extractAEPower(aeCost, Actionable.MODULATE, PowerMultiplier.ONE);
        if (extractedPower + 1.0e-7 < aeCost) {
            if (extractedFluid > 0) storage.insert(fluidKey, extractedFluid, Actionable.MODULATE, source);
            if (extractedPower > 0) grid.getEnergyService().injectPower(extractedPower, Actionable.MODULATE);
            return false;
        }

        host.jdtta$setFluidRemainder((host.jdtta$getFluidRemainder() + fullFluidCost) % WAND_DURATION);
        MiscTools.doExtraTicks(level, targetPos, rate);
        return true;
    }

    private static int cumulativeFluidCost(int rate) {
        int result = 0;
        for (int wandRate = 2; wandRate <= rate; wandRate *= 2) {
            result += (int) (wandRate * TimeWand.getMBPerRate());
            if (wandRate > Integer.MAX_VALUE / 2) break;
        }
        return result;
    }

    private static boolean isMachineWorking(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && Boolean.TRUE.equals(getMachineWorkingState(be));
    }

    @Nullable
    private static Boolean getMachineWorkingState(BlockEntity be) {
        for (Direction side : Direction.values()) {
            var crafting = ICraftingMachine.of(be, side);
            if (crafting != null && !crafting.acceptsPlans()) return true;
        }
        Method method = WORK_METHODS.computeIfAbsent(be.getClass(), AE2AccelerationEngine::findWorkMethod);
        if (method == null) return null;
        try { return Boolean.TRUE.equals(method.invoke(be)); }
        catch (ReflectiveOperationException ignored) { return null; }
    }

    @Nullable
    private static Method findWorkMethod(Class<?> type) {
        for (String name : new String[]{"isWorking", "isRunning", "isProcessing"}) {
            try {
                Method method = type.getMethod(name);
                if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) return method;
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }
}
