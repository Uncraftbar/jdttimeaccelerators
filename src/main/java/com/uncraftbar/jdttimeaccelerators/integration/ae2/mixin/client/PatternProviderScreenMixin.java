package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2AccelerationButton;

@Mixin(value = PatternProviderScreen.class, remap = false)
public abstract class PatternProviderScreenMixin {
    @Unique private AE2AccelerationButton jdtta$speedButton;
    @Unique private AE2AccelerationButton jdtta$conditionalButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$addControls(PatternProviderMenu menu, Inventory inventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        var access = (AEBaseScreenAccessor) this;
        // AppliedFlux already adds the shared `upgrades` panel to every
        // PatternProviderScreen. This includes AE2 itself and subclassed provider
        // screens from ExtendedAE, MEGA Cells, and AdvancedAE. Widget IDs must be
        // unique, so only provide our own panel when AppliedFlux is absent.
        if (!ModList.get().isLoaded("appflux")) {
            access.jdtta$getWidgets().add("upgrades", new UpgradesPanel(
                    menu.getSlots(SlotSemantics.UPGRADE),
                    () -> List.of(Component.translatable("gui.jdttimeaccelerators.ae2.card_slot"))));
        }
        jdtta$speedButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.SPEED, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.CYCLE_SPEED, Screen.hasShiftDown())));
        jdtta$conditionalButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.MODE, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.TOGGLE_CONDITIONAL, false)));
        access.jdtta$getVerticalToolbar().add(jdtta$speedButton);
        access.jdtta$getVerticalToolbar().add(jdtta$conditionalButton);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void jdtta$updateControls(CallbackInfo ci) {
        var menu = (AE2AccelerationMenu) ((PatternProviderScreen<?>) (Object) this).getMenu();
        boolean visible = menu.jdtta$getSyncedCardInstalled();
        jdtta$speedButton.setVisibility(visible);
        jdtta$conditionalButton.setVisibility(visible);
        jdtta$speedButton.setState(Component.literal((1 << menu.jdtta$getSyncedSpeedLevel()) + "x"), false);
        jdtta$conditionalButton.setState(Component.translatable(menu.jdtta$getSyncedConditional()
                ? "gui.jdttimeaccelerators.ae2.requested_only"
                : "gui.jdttimeaccelerators.ae2.always"), menu.jdtta$getSyncedConditional());
    }
}
