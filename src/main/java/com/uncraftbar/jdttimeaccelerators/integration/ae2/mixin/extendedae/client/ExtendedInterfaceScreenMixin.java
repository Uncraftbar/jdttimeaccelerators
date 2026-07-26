package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.extendedae.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2AccelerationButton;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2TargetSideScreen;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.client.AEBaseScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Optional controls for ExtendedAE's custom interface screen. */
@Mixin(targets = "com.glodblock.github.extendedae.client.gui.GuiExInterface", remap = false)
public abstract class ExtendedInterfaceScreenMixin {
    @Unique private AE2AccelerationButton jdtta$speedButton;
    @Unique private AE2AccelerationButton jdtta$conditionalButton;
    @Unique private AE2AccelerationButton jdtta$targetButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$addControls(CallbackInfo ci) {
        var toolbar = ((AEBaseScreenAccessor) this).jdtta$getVerticalToolbar();
        jdtta$speedButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.SPEED, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.CYCLE_SPEED, Screen.hasShiftDown())));
        jdtta$conditionalButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.INTERFACE_MODE, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.TOGGLE_CONDITIONAL, false)));
        jdtta$targetButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.TARGET, button -> {
            var screen = (AEBaseScreen<?>) (Object) this;
            AE2TargetSideScreen.open(screen, (AE2AccelerationMenu) screen.getMenu());
        });
        toolbar.add(jdtta$speedButton);
        toolbar.add(jdtta$conditionalButton);
        toolbar.add(jdtta$targetButton);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void jdtta$updateControls(CallbackInfo ci) {
        var screen = (AEBaseScreen<?>) (Object) this;
        var menu = (AE2AccelerationMenu) screen.getMenu();
        boolean visible = menu.jdtta$getSyncedCardInstalled();
        jdtta$speedButton.setVisibility(visible);
        jdtta$conditionalButton.setVisibility(visible);
        jdtta$targetButton.setVisibility(visible && menu.jdtta$getSyncedTargetConfigurable());
        jdtta$speedButton.setState(Component.literal((1 << menu.jdtta$getSyncedSpeedLevel()) + "x"), false);
        jdtta$conditionalButton.setState(Component.translatable(menu.jdtta$getSyncedConditional()
                ? "gui.jdttimeaccelerators.ae2.redstone_signal"
                : "gui.jdttimeaccelerators.ae2.always"), menu.jdtta$getSyncedConditional());
        int selected = Integer.bitCount(menu.jdtta$getSyncedTargetMask());
        jdtta$targetButton.setState(Component.literal(selected == 6 ? "All" : Integer.toString(selected)),
                Component.translatable("gui.jdttimeaccelerators.ae2.target_count", selected), false);
    }
}
