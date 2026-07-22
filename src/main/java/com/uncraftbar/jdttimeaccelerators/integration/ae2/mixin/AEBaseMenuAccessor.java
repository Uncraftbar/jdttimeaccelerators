package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.AEBaseMenu;

/** Accesses AE2's protected menu helper without copying its upgrade-slot rules. */
@Mixin(value = AEBaseMenu.class, remap = false)
public interface AEBaseMenuAccessor {
    @Invoker("setupUpgrades")
    void jdtta$setupUpgrades(IUpgradeInventory upgrades);
}
