package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import java.util.EnumSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.parts.misc.InterfacePart;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

@Mixin(value = InterfaceLogic.class, remap = false)
public abstract class InterfaceLogicMixin implements AE2AccelerationHost {
    @Shadow @Final protected InterfaceLogicHost host;
    @Shadow @Final protected IManagedGridNode mainNode;
    @Shadow @Final protected IActionSource actionSource;
    @Shadow @Final private IUpgradeInventory upgrades;

    @Unique private int jdtta$speedLevel = 1;
    @Unique private boolean jdtta$conditional;
    @Unique private int jdtta$fluidRemainder = 599;

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void jdtta$write(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt("jdttaSpeedLevel", jdtta$speedLevel);
        tag.putBoolean("jdttaConditional", jdtta$conditional);
        tag.putInt("jdttaFluidRemainder", jdtta$fluidRemainder);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void jdtta$read(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        jdtta$speedLevel = Math.max(1, tag.getInt("jdttaSpeedLevel"));
        jdtta$conditional = tag.getBoolean("jdttaConditional");
        jdtta$fluidRemainder = tag.contains("jdttaFluidRemainder") ? tag.getInt("jdttaFluidRemainder") : 599;
    }

    @Override public IManagedGridNode jdtta$getMainNode() { return mainNode; }
    @Override public IActionSource jdtta$getActionSource() { return actionSource; }
    @Override public IUpgradeInventory jdtta$getAccelerationUpgrades() { return upgrades; }
    @Override public net.minecraft.world.level.block.entity.BlockEntity jdtta$getHostBlockEntity() { return host.getBlockEntity(); }
    @Override public Set<Direction> jdtta$getTargetDirections() {
        if (host instanceof InterfacePart part) return EnumSet.of(part.getSide());
        return EnumSet.allOf(Direction.class);
    }
    @Override public int jdtta$getSpeedLevel() { return jdtta$speedLevel; }
    @Override public void jdtta$setSpeedLevel(int level) { jdtta$speedLevel = Math.max(1, Math.min(level, AE2AccelerationEngine.maxSpeedLevel())); host.saveChanges(); }
    @Override public boolean jdtta$isConditional() { return jdtta$conditional; }
    @Override public void jdtta$setConditional(boolean conditional) { jdtta$conditional = conditional; host.saveChanges(); }
    @Override public int jdtta$getFluidRemainder() { return jdtta$fluidRemainder; }
    @Override public void jdtta$setFluidRemainder(int remainder) { jdtta$fluidRemainder = remainder; host.saveChanges(); }
    @Override public @Nullable BlockPos jdtta$getRequestedTarget() { return null; }
    @Override public void jdtta$setRequestedTarget(@Nullable BlockPos pos, long expiry) {}
    @Override public long jdtta$getRequestedTargetExpiry() { return 0; }
    @Override public int jdtta$getRequestedTargetIdleTicks() { return 0; }
    @Override public void jdtta$setRequestedTargetIdleTicks(int ticks) {}
    @Override public void jdtta$saveChanges() { host.saveChanges(); }
}
