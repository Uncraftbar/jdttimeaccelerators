package com.uncraftbar.jdttimeaccelerators.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.uncraftbar.jdttimeaccelerators.common.blockentities.TimeAcceleratorT1BE;
import com.uncraftbar.jdttimeaccelerators.common.network.data.TimeAcceleratorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class TimeAcceleratorScreen<T extends BaseMachineContainer> extends BaseMachineScreen<T> {
    private static final ResourceLocation ADD_BUTTON = ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/add.png");
    private static final ResourceLocation REMOVE_BUTTON = ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/remove.png");
    protected int speedLevel = 1;

    public TimeAcceleratorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
        if (baseMachineBE instanceof TimeAcceleratorT1BE acceleratorBE)
            speedLevel = acceleratorBE.getSpeedLevel();
    }

    @Override
    public void setTopSection() {
        // Match the wider Advanced Fluid Placer style for area machines so the Rad/Off
        // controls are not cramped against energy/fluid bars or utility buttons.
        extraWidth = baseMachineBE instanceof AreaAffectingBE ? 60 : 20;
        extraHeight = 0;
    }

    @Override
    public void addTickSpeedButton() {
        // Time Accelerators use the Time Wand multiplier only. The inherited machine tick-speed
        // control would compound with this and make speeds like 2x depend on a hidden second setting.
    }

    @Override
    public void init() {
        super.init();
        int controlWidth = 70;
        int controlX = getGuiLeft() + (imageWidth / 2) - (controlWidth / 2);
        int controlY = baseMachineBE instanceof AreaAffectingBE ? topSectionTop + 53 : topSectionTop + 45;

        addRenderableWidget(new GrayscaleButton(controlX, controlY, 12, 12, REMOVE_BUTTON, button -> changeSpeedLevel(-1)));
        addRenderableWidget(new GrayscaleButton(controlX + controlWidth - 12, controlY, 12, 12, ADD_BUTTON, button -> changeSpeedLevel(1)));
    }

    protected void changeSpeedLevel(int change) {
        speedLevel = Math.max(1, Math.min(speedLevel + change, getMaxSpeedLevel()));
        PacketDistributor.sendToServer(new TimeAcceleratorPayload(speedLevel));
    }

    protected int getMaxSpeedLevel() {
        if (baseMachineBE instanceof TimeAcceleratorT1BE acceleratorBE)
            return Math.max(1, acceleratorBE.getMaxSpeedLevel());
        return 1;
    }

    protected int getDisplayedMultiplier() {
        if (baseMachineBE instanceof TimeAcceleratorT1BE acceleratorBE)
            return acceleratorBE.getAccelerationRateForSpeedLevel(speedLevel);
        return 1 << speedLevel;
    }

    @Override
    public int getFluidBarOffset() {
        return baseMachineBE instanceof AreaAffectingBE ? 204 : super.getFluidBarOffset();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int centerX = (imageWidth / 2);
        int labelY = baseMachineBE instanceof AreaAffectingBE ? topSectionTop - getGuiTop() + 44 : topSectionTop - getGuiTop() + 36;
        Component label = Component.literal("Wand Speed");
        Component multiplier = Component.literal(getDisplayedMultiplier() + "x");
        guiGraphics.drawString(this.font, label, centerX - this.font.width(label) / 2, labelY, 4210752, false);
        guiGraphics.drawString(this.font, multiplier, centerX - this.font.width(multiplier) / 2, labelY + 12, 4210752, false);
    }
}
