package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class PatternProviderLogicMixin implements AE2AccelerationHost {
    @Shadow @Final private PatternProviderLogicHost host;
    @Shadow @Final private IManagedGridNode mainNode;
    @Shadow @Final private IActionSource actionSource;
    @Shadow private @Nullable Direction sendDirection;
    @Shadow protected abstract boolean sendStacksOut();

    @Unique private IUpgradeInventory jdtta$accelerationUpgrades;
    @Unique private int jdtta$speedLevel = 1;
    @Unique private boolean jdtta$conditional;
    @Unique private int jdtta$fluidRemainder = 599;
    @Unique private BlockPos jdtta$requestedTarget;
    @Unique private long jdtta$requestedTargetExpiry;
    @Unique private int jdtta$requestedTargetIdleTicks;
    @Unique private BlockPos jdtta$pushCandidateTarget;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void jdtta$initUpgrades(IManagedGridNode node, PatternProviderLogicHost host, int slots, CallbackInfo ci) {
        this.jdtta$accelerationUpgrades = UpgradeInventories.forMachine(host.getTerminalIcon().getItem(), 1, () -> {
            host.saveChanges();
            mainNode.ifPresent((grid, gridNode) -> grid.getTickManager().alertDevice(gridNode));
        });
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void jdtta$write(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (jdtta$accelerationUpgrades == null) return;
        jdtta$accelerationUpgrades.writeToNBT(tag, "jdttaAccelerationUpgrades", registries);
        tag.putInt("jdttaSpeedLevel", jdtta$speedLevel);
        tag.putBoolean("jdttaConditional", jdtta$conditional);
        tag.putInt("jdttaFluidRemainder", jdtta$fluidRemainder);
        if (jdtta$requestedTarget != null) tag.putLong("jdttaRequestedTarget", jdtta$requestedTarget.asLong());
        tag.putLong("jdttaRequestedTargetExpiry", jdtta$requestedTargetExpiry);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void jdtta$read(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (jdtta$accelerationUpgrades == null) return;
        jdtta$accelerationUpgrades.readFromNBT(tag, "jdttaAccelerationUpgrades", registries);
        jdtta$speedLevel = Math.max(1, tag.getInt("jdttaSpeedLevel"));
        jdtta$conditional = tag.getBoolean("jdttaConditional");
        jdtta$fluidRemainder = tag.contains("jdttaFluidRemainder") ? tag.getInt("jdttaFluidRemainder") : 599;
        jdtta$requestedTarget = tag.contains("jdttaRequestedTarget") ? BlockPos.of(tag.getLong("jdttaRequestedTarget")) : null;
        jdtta$requestedTargetExpiry = tag.getLong("jdttaRequestedTargetExpiry");
    }

    /**
     * Capture the accepted output face immediately before AE2 empties it. This hook remains
     * valid when ExpandedAE replaces pushPattern entirely, and is also shared by ExtendedAE
     * and MEGA Cells because those providers reuse AE2's PatternProviderLogic.
     */
    @Inject(method = "sendStacksOut", at = @At("HEAD"))
    private void jdtta$captureAcceptedOutput(CallbackInfoReturnable<Boolean> cir) {
        var be = host.getBlockEntity();
        var level = be.getLevel();
        if (level == null || sendDirection == null) return;
        jdtta$requestedTarget = be.getBlockPos().relative(sendDirection);
        jdtta$requestedTargetExpiry = level.getGameTime() + AE2AccelerationEngine.requestedStaleTimeout();
        jdtta$resetRequestedTracking();
        host.saveChanges();
    }

    @Unique
    private void jdtta$resetRequestedTracking() {
        jdtta$requestedTargetIdleTicks = 0;
    }

    @Inject(method = "addDrops", at = @At("TAIL"))
    private void jdtta$addDrops(List<ItemStack> drops, CallbackInfo ci) {
        if (jdtta$accelerationUpgrades == null) return;
        for (ItemStack stack : jdtta$accelerationUpgrades) if (!stack.isEmpty()) drops.add(stack.copy());
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void jdtta$clear(CallbackInfo ci) {
        if (jdtta$accelerationUpgrades != null) jdtta$accelerationUpgrades.clear();
    }

    @Override public IManagedGridNode jdtta$getMainNode() { return mainNode; }
    @Override public IActionSource jdtta$getActionSource() { return actionSource; }
    @Override public IUpgradeInventory jdtta$getAccelerationUpgrades() { return jdtta$accelerationUpgrades; }
    @Override public net.minecraft.world.level.block.entity.BlockEntity jdtta$getHostBlockEntity() { return host.getBlockEntity(); }
    @Override public Set<Direction> jdtta$getTargetDirections() { return host.getTargets(); }
    @Override public int jdtta$getSpeedLevel() { return jdtta$speedLevel; }
    @Override public void jdtta$setSpeedLevel(int level) { jdtta$speedLevel = Math.max(1, Math.min(level, AE2AccelerationEngine.maxSpeedLevel())); host.saveChanges(); }
    @Override public boolean jdtta$isConditional() { return jdtta$conditional; }
    @Override public void jdtta$setConditional(boolean conditional) { jdtta$conditional = conditional; host.saveChanges(); }
    @Override public int jdtta$getFluidRemainder() { return jdtta$fluidRemainder; }
    @Override public void jdtta$setFluidRemainder(int remainder) { jdtta$fluidRemainder = remainder; host.saveChanges(); }
    @Override public boolean jdtta$isPatternProvider() { return true; }
    @Override public @Nullable BlockPos jdtta$getRequestedTarget() { return jdtta$requestedTarget; }
    @Override public void jdtta$setRequestedTarget(@Nullable BlockPos pos, long expiry) { jdtta$requestedTarget = pos; jdtta$requestedTargetExpiry = expiry; }
    @Override public long jdtta$getRequestedTargetExpiry() { return jdtta$requestedTargetExpiry; }
    @Override public int jdtta$getRequestedTargetIdleTicks() { return jdtta$requestedTargetIdleTicks; }
    @Override public void jdtta$setRequestedTargetIdleTicks(int ticks) { jdtta$requestedTargetIdleTicks = ticks; }
    @Override public void jdtta$saveChanges() { host.saveChanges(); }
}
