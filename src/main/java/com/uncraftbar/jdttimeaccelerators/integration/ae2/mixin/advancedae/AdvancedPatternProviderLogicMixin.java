package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.advancedae;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.parts.AEBasePart;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Optional bridge for AdvancedAE's independent pattern-provider implementation. */
@Mixin(targets = "net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic", remap = false)
public abstract class AdvancedPatternProviderLogicMixin implements AE2AccelerationHost {
    @Unique private IUpgradeInventory jdtta$accelerationUpgrades;
    @Unique private int jdtta$speedLevel = 1;
    @Unique private boolean jdtta$conditional;
    @Unique private int jdtta$fluidRemainder = 599;
    @Unique private int jdtta$targetMask = AE2AccelerationTarget.ALL_MASK;
    @Unique private BlockPos jdtta$requestedTarget;
    @Unique private long jdtta$requestedTargetExpiry;
    @Unique private int jdtta$requestedTargetIdleTicks;
    @Unique private BlockPos jdtta$pushCandidateTarget;
    @Unique private Object jdtta$cachedHost;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void jdtta$initialize(CallbackInfo ci) {
        Object host = jdtta$host();
        Item machine = (Item) jdtta$invoke(jdtta$invoke(host, "getTerminalIcon"), "getItem");
        jdtta$accelerationUpgrades = UpgradeInventories.forMachine(machine, 1, () -> {
            jdtta$saveChanges();
            jdtta$getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        });
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void jdtta$write(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (jdtta$accelerationUpgrades == null) return;
        jdtta$accelerationUpgrades.writeToNBT(tag, "jdttaAccelerationUpgrades", registries);
        tag.putInt("jdttaSpeedLevel", jdtta$speedLevel);
        tag.putBoolean("jdttaConditional", jdtta$conditional);
        tag.putInt("jdttaFluidRemainder", jdtta$fluidRemainder);
        tag.putInt("jdttaTargetMask", jdtta$targetMask);
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
        jdtta$targetMask = tag.contains("jdttaTargetMask")
                ? AE2AccelerationTarget.sanitize(tag.getInt("jdttaTargetMask"))
                : AE2AccelerationTarget.ALL_MASK;
        jdtta$requestedTarget = tag.contains("jdttaRequestedTarget") ? BlockPos.of(tag.getLong("jdttaRequestedTarget")) : null;
        jdtta$requestedTargetExpiry = tag.getLong("jdttaRequestedTargetExpiry");
    }

    @Inject(method = "pushPattern", at = @At("HEAD"))
    private void jdtta$beginPush(IPatternDetails pattern, KeyCounter[] inputs,
            CallbackInfoReturnable<Boolean> cir) {
        jdtta$pushCandidateTarget = null;
    }

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target =
            "Lappeng/api/implementations/blockentities/ICraftingMachine;of(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lappeng/api/implementations/blockentities/ICraftingMachine;"))
    private ICraftingMachine jdtta$captureProbedTarget(Level level, BlockPos pos, Direction side) {
        jdtta$pushCandidateTarget = pos;
        return ICraftingMachine.of(level, pos, side);
    }

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target =
            "Lappeng/api/implementations/blockentities/ICraftingMachine;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;Lnet/minecraft/core/Direction;)Z"))
    private boolean jdtta$captureDirectPush(ICraftingMachine machine, IPatternDetails pattern,
            KeyCounter[] inputs, Direction side) {
        boolean accepted = machine.pushPattern(pattern, inputs, side);
        if (accepted && jdtta$pushCandidateTarget != null) jdtta$rememberTarget(jdtta$pushCandidateTarget);
        return accepted;
    }

    @Inject(method = "pushPattern", at = @At(value = "INVOKE", target =
            "Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogic;sendStacksOut()Z"))
    private void jdtta$captureAdapterPush(IPatternDetails pattern, KeyCounter[] inputs,
            CallbackInfoReturnable<Boolean> cir) {
        Direction direction = (Direction) jdtta$field("sendDirection");
        if (direction != null) jdtta$rememberTarget(jdtta$getHostBlockEntity().getBlockPos().relative(direction));
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

    @Unique private void jdtta$rememberTarget(BlockPos target) {
        BlockEntity be = jdtta$getHostBlockEntity();
        if (be.getLevel() == null) return;
        jdtta$requestedTarget = target;
        jdtta$requestedTargetExpiry = be.getLevel().getGameTime() + AE2AccelerationEngine.requestedStaleTimeout();
        jdtta$requestedTargetIdleTicks = 0;
        jdtta$saveChanges();
        AE2AccelerationEngine.alertTicker(this);
    }

    @Unique private Object jdtta$host() {
        if (jdtta$cachedHost == null) jdtta$cachedHost = jdtta$field("host");
        return jdtta$cachedHost;
    }

    @Unique private Object jdtta$field(String name) {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("AdvancedAE compatibility field missing: " + name, e);
        }
    }

    @Unique private static Object jdtta$invoke(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
            throw new IllegalStateException("AdvancedAE compatibility method missing: " + name, cause);
        }
    }

    @Override public IManagedGridNode jdtta$getMainNode() { return (IManagedGridNode) jdtta$field("mainNode"); }
    @Override public IActionSource jdtta$getActionSource() { return (IActionSource) jdtta$field("actionSource"); }
    @Override public IUpgradeInventory jdtta$getAccelerationUpgrades() { return jdtta$accelerationUpgrades; }
    @Override public @Nullable IUpgradeInventory jdtta$getExternalUpgrades() {
        Object providerHost = jdtta$host();
        return providerHost instanceof IUpgradeableObject upgradeable ? upgradeable.getUpgrades() : null;
    }
    @Override public BlockEntity jdtta$getHostBlockEntity() { return (BlockEntity) jdtta$invoke(jdtta$host(), "getBlockEntity"); }
    @SuppressWarnings("unchecked")
    @Override public Set<Direction> jdtta$getTargetDirections() { return (Set<Direction>) jdtta$invoke(jdtta$host(), "getTargets"); }
    @Override public boolean jdtta$isTargetSelectionConfigurable() { return !(jdtta$host() instanceof AEBasePart); }
    @Override public int jdtta$getTargetMask() { return jdtta$targetMask; }
    @Override public void jdtta$setTargetMask(int mask) {
        jdtta$targetMask = AE2AccelerationTarget.sanitize(mask);
        jdtta$saveChanges();
    }
    @Override public int jdtta$getSpeedLevel() { return jdtta$speedLevel; }
    @Override public void jdtta$setSpeedLevel(int level) { jdtta$speedLevel = Math.max(1, Math.min(level, AE2AccelerationEngine.maxSpeedLevel())); jdtta$saveChanges(); }
    @Override public boolean jdtta$isConditional() { return jdtta$conditional; }
    @Override public void jdtta$setConditional(boolean conditional) { jdtta$conditional = conditional; jdtta$saveChanges(); }
    @Override public int jdtta$getFluidRemainder() { return jdtta$fluidRemainder; }
    @Override public void jdtta$setFluidRemainder(int remainder) { jdtta$fluidRemainder = remainder; jdtta$saveChanges(); }
    @Override public boolean jdtta$isPatternProvider() { return true; }
    @Override public @Nullable BlockPos jdtta$getRequestedTarget() { return jdtta$requestedTarget; }
    @Override public void jdtta$setRequestedTarget(@Nullable BlockPos pos, long expiry) { jdtta$requestedTarget = pos; jdtta$requestedTargetExpiry = expiry; }
    @Override public long jdtta$getRequestedTargetExpiry() { return jdtta$requestedTargetExpiry; }
    @Override public int jdtta$getRequestedTargetIdleTicks() { return jdtta$requestedTargetIdleTicks; }
    @Override public void jdtta$setRequestedTargetIdleTicks(int ticks) { jdtta$requestedTargetIdleTicks = ticks; }
    @Override public void jdtta$saveChanges() { jdtta$invoke(jdtta$host(), "saveChanges"); }
}
