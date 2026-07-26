package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.InterfaceMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2AccelerationButton;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.client.AE2TargetSideScreen;

@Mixin(value = InterfaceScreen.class, remap = false)
public abstract class InterfaceScreenMixin {
    @Unique private AE2AccelerationButton jdtta$speedButton;
    @Unique private AE2AccelerationButton jdtta$conditionalButton;
    @Unique private AE2AccelerationButton jdtta$targetButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$addControls(InterfaceMenu menu, Inventory inventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        var toolbar = ((AEBaseScreenAccessor) this).jdtta$getVerticalToolbar();
        jdtta$speedButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.SPEED, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.CYCLE_SPEED, Screen.hasShiftDown())));
        jdtta$conditionalButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.INTERFACE_MODE, button ->
                PacketDistributor.sendToServer(new AE2AccelerationPayload(
                        AE2AccelerationPayload.TOGGLE_CONDITIONAL, false)));
        jdtta$targetButton = new AE2AccelerationButton(AE2AccelerationButton.Kind.TARGET, button -> {
            var screen = (InterfaceScreen<?>) (Object) this;
            AE2TargetSideScreen.open(screen, (AE2AccelerationMenu) screen.getMenu());
        });
        toolbar.add(jdtta$speedButton);
        toolbar.add(jdtta$conditionalButton);
        toolbar.add(jdtta$targetButton);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void jdtta$updateControls(CallbackInfo ci) {
        var menu = (AE2AccelerationMenu) ((InterfaceScreen<?>) (Object) this).getMenu();
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
