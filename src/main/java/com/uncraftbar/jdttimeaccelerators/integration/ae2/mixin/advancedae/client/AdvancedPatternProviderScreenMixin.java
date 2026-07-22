package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.advancedae.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.client.gui.AEBaseScreen;
import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2AccelerationButton;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.client.AEBaseScreenAccessor;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(targets = {
        "net.pedroksl.advanced_ae.client.gui.AdvPatternProviderScreen",
        "net.pedroksl.advanced_ae.client.gui.SmallAdvPatternProviderScreen"
}, remap = false)
public abstract class AdvancedPatternProviderScreenMixin {
    @Unique private AE2AccelerationButton jdtta$speedButton;
    @Unique private AE2AccelerationButton jdtta$conditionalButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$addControls(CallbackInfo ci) {
        var screen = (AEBaseScreen<?>) (Object) this;
        // AdvancedAE's AppFlux compatibility mixin already installs the "upgrades"
        // panel for both advanced provider screens. Reuse it: WidgetContainer IDs
        // must be unique, and registering a second panel disconnects the client.
        var access = (AEBaseScreenAccessor) this;
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
        var menu = (AE2AccelerationMenu) ((AEBaseScreen<?>) (Object) this).getMenu();
        // SmallAdvPatternProviderMenu rebuilds its upgrade slots after the
        // superclass constructor. The slots themselves are authoritative on the
        // client, so use them as a fallback if its GuiSync value trails behind.
        var screen = (AEBaseScreen<?>) (Object) this;
        boolean cardInVisibleSlot = screen.getMenu().slots.stream()
                .anyMatch(slot -> slot.getItem().is(Registration.AE2_TIME_ACCELERATION_CARD.get()));
        boolean visible = menu.jdtta$getSyncedCardInstalled() || cardInVisibleSlot;
        jdtta$speedButton.setVisibility(visible);
        jdtta$conditionalButton.setVisibility(visible);
        jdtta$speedButton.setState(Component.literal((1 << menu.jdtta$getSyncedSpeedLevel()) + "x"), false);
        jdtta$conditionalButton.setState(Component.translatable(menu.jdtta$getSyncedConditional()
                ? "gui.jdttimeaccelerators.ae2.requested_only"
                : "gui.jdttimeaccelerators.ae2.always"), menu.jdtta$getSyncedConditional());
    }
}
