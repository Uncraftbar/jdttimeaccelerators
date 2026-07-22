package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Makes the provider's acceleration upgrade win over its generic return inventory. */
@Mixin(value = AEBaseMenu.class, remap = false)
public abstract class PatternProviderQuickMoveMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void jdtta$quickMoveAccelerationCard(Player player, int index,
            CallbackInfoReturnable<ItemStack> cir) {
        AEBaseMenu menu = (AEBaseMenu) (Object) this;
        if (!(menu instanceof com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu)
                || index < 0 || index >= menu.slots.size()) return;
        var source = menu.slots.get(index);
        if (!menu.isPlayerSideSlot(source)) return;
        ItemStack original = source.getItem();
        if (!original.is(Registration.AE2_TIME_ACCELERATION_CARD.get())) return;

        ItemStack remainder = original.copy();
        for (var upgradeSlot : menu.getSlots(SlotSemantics.UPGRADE)) {
            remainder = upgradeSlot.safeInsert(remainder);
            if (remainder.isEmpty()) break;
        }
        int moved = original.getCount() - remainder.getCount();
        if (moved <= 0) return;
        source.remove(moved);
        source.setChanged();
        menu.broadcastChanges();
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
