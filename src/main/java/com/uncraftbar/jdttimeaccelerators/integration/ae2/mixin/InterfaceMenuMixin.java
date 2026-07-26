package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.helpers.InterfaceLogicHost;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;

@Mixin(value = UpgradeableMenu.class, remap = false)
public abstract class InterfaceMenuMixin implements AE2AccelerationMenu {
    @Unique private AE2AccelerationHost jdtta$host;

    @GuiSync(40) public int jdtta$speedLevel = 1;
    @GuiSync(41) public boolean jdtta$conditional;
    @GuiSync(42) public boolean jdtta$cardInstalled;
    @GuiSync(43) public int jdtta$targetMask;
    @GuiSync(44) public boolean jdtta$targetConfigurable;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$initialize(MenuType<?> type, int id, Inventory inventory,
            appeng.api.upgrades.IUpgradeableObject host, CallbackInfo ci) {
        // Vanilla AE2, MEGA Cells and ExtendedAE all pass an InterfaceLogicHost
        // through UpgradeableMenu, but only vanilla/MEGA use InterfaceMenu itself.
        // Key off the host contract rather than one concrete menu class so addon
        // interfaces receive the same sync, controls and packet handling.
        if (host instanceof InterfaceLogicHost interfaceHost) {
            this.jdtta$host = (AE2AccelerationHost) (Object) interfaceHost.getInterfaceLogic();
        }
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void jdtta$sync(CallbackInfo ci) {
        if (jdtta$host != null) {
            jdtta$speedLevel = jdtta$host.jdtta$getSpeedLevel();
            jdtta$conditional = jdtta$host.jdtta$isConditional();
            jdtta$cardInstalled = AE2AccelerationEngine.isCardInstalled(jdtta$host);
            jdtta$targetMask = jdtta$host.jdtta$getTargetMask();
            jdtta$targetConfigurable = jdtta$host.jdtta$isTargetSelectionConfigurable();
            // AppliedFlux owns its original slot and only wakes its own energy ticker.
            // Alert AE2's provider/interface ticker as well so a card inserted there
            // starts acceleration immediately, just like one inserted in our slot.
            if (jdtta$cardInstalled) AE2AccelerationEngine.alertTicker(jdtta$host);
        }
    }

    @Override public int jdtta$getSyncedSpeedLevel() { return jdtta$speedLevel; }
    @Override public boolean jdtta$getSyncedConditional() { return jdtta$conditional; }
    @Override public boolean jdtta$getSyncedCardInstalled() { return jdtta$cardInstalled; }
    @Override public int jdtta$getSyncedTargetMask() { return jdtta$targetMask; }
    @Override public boolean jdtta$getSyncedTargetConfigurable() { return jdtta$targetConfigurable; }
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
    @Override public net.minecraft.world.level.block.entity.BlockEntity jdtta$getHostBlockEntity() {
        return jdtta$host.jdtta$getHostBlockEntity();
    }
}
