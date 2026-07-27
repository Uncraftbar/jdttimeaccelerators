package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.advancedae;

import java.lang.reflect.Field;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.menu.guisync.GuiSync;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.AEBaseMenuAccessor;

@Mixin(targets = "net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu", remap = false)
public abstract class AdvancedPatternProviderMenuMixin implements AE2AccelerationMenu {
    @Unique private AE2AccelerationHost jdtta$host;
    @GuiSync(40) public int jdtta$speedLevel = 1;
    @GuiSync(41) public boolean jdtta$conditional;
    @GuiSync(42) public boolean jdtta$cardInstalled;
    @GuiSync(43) public int jdtta$targetMask;
    @GuiSync(44) public boolean jdtta$targetConfigurable;
    @GuiSync(45) public boolean jdtta$acceptedIngredientsOnly;

    /*
     * Target only AdvancedAE's protected implementation constructor. Its public
     * constructor delegates to this one, and a wildcard constructor injection ran
     * twice for the full-size menu. The small menu also delegates here, so it does
     * not need a second concrete-menu injection.
     */
    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;)V", at = @At("TAIL"))
    private void jdtta$initialize(CallbackInfo ci) {
        try {
            Class<?> type = this.getClass();
            Field field = null;
            while (type != null && field == null) {
                try {
                    field = type.getDeclaredField("logic");
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            if (field == null) throw new NoSuchFieldException("logic");
            field.setAccessible(true);
            jdtta$host = (AE2AccelerationHost) field.get(this);
            ((AEBaseMenuAccessor) this).jdtta$setupUpgrades(jdtta$host.jdtta$getAccelerationUpgrades());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("AdvancedAE menu logic field missing", e);
        }
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void jdtta$sync(CallbackInfo ci) {
        if (jdtta$host == null) return;
        jdtta$speedLevel = jdtta$host.jdtta$getSpeedLevel();
        jdtta$conditional = jdtta$host.jdtta$isConditional();
        jdtta$cardInstalled = AE2AccelerationEngine.isCardInstalled(jdtta$host);
        jdtta$targetMask = jdtta$host.jdtta$getTargetMask();
        jdtta$targetConfigurable = jdtta$host.jdtta$isTargetSelectionConfigurable();
        jdtta$acceptedIngredientsOnly = jdtta$host.jdtta$isAcceptedIngredientsOnly();
            // AppliedFlux owns its original slot and only wakes its own energy ticker.
            // Alert AE2's provider/interface ticker as well so a card inserted there
            // starts acceleration immediately, just like one inserted in our slot.
            if (jdtta$cardInstalled) AE2AccelerationEngine.alertTicker(jdtta$host);
    }

    @Override public int jdtta$getSyncedSpeedLevel() { return jdtta$speedLevel; }
    @Override public boolean jdtta$getSyncedConditional() { return jdtta$conditional; }
    @Override public boolean jdtta$getSyncedCardInstalled() { return jdtta$cardInstalled; }
    @Override public int jdtta$getSyncedTargetMask() { return jdtta$targetMask; }
    @Override public boolean jdtta$getSyncedTargetConfigurable() { return jdtta$targetConfigurable; }
    @Override public boolean jdtta$getSyncedAcceptedIngredientsOnly() { return jdtta$acceptedIngredientsOnly; }
    @Override public boolean jdtta$isPatternProviderMenu() { return true; }
    @Override public void jdtta$cycleSpeed(boolean backwards) {
        if (jdtta$host == null) return;
        int max = AE2AccelerationEngine.maxSpeedLevel();
        int next = jdtta$host.jdtta$getSpeedLevel() + (backwards ? -1 : 1);
        if (next < 1) next = max;
        if (next > max) next = 1;
        jdtta$host.jdtta$setSpeedLevel(next);
    }
    @Override public void jdtta$toggleConditional() {
        if (jdtta$host != null) jdtta$host.jdtta$setConditional(!jdtta$host.jdtta$isConditional());
    }
    @Override public void jdtta$setTargetMask(int mask) {
        if (jdtta$host == null || !jdtta$host.jdtta$isTargetSelectionConfigurable()) return;
        jdtta$host.jdtta$setTargetMask(mask);
    }
    @Override public void jdtta$toggleAcceptedIngredientsOnly() {
        if (jdtta$host != null) {
            jdtta$host.jdtta$setAcceptedIngredientsOnly(
                    !jdtta$host.jdtta$isAcceptedIngredientsOnly());
        }
    }
    @Override public net.minecraft.world.level.block.entity.BlockEntity jdtta$getHostBlockEntity() {
        return jdtta$host.jdtta$getHostBlockEntity();
    }
}
