package com.uncraftbar.jdttimeaccelerators.integration.ae2.client;

import java.util.List;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * A proper AE2 toolbar button, following the same IconButton pattern used by
 * AE2 Import Export Card. The toolbar only supports 16x16 controls; putting
 * normal, wide text buttons in it produces the stray horizontal bars.
 */
public final class AE2AccelerationButton extends IconButton {
    public enum Kind {
        SPEED,
        MODE
    }

    private final Kind kind;
    private Component state = Component.empty();
    private boolean conditional;

    public AE2AccelerationButton(Kind kind, OnPress onPress) {
        super(onPress);
        this.kind = kind;
    }

    public void setState(Component state, boolean conditional) {
        this.state = state;
        this.conditional = conditional;
    }

    @Override
    protected Icon getIcon() {
        if (kind == Kind.MODE) {
            return conditional ? Icon.CRAFT_HAMMER : Icon.REDSTONE_IGNORE;
        }
        // Speed draws its multiplier directly so the current setting is visible.
        return null;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        int yOffset = isHoveredOrFocused() ? 1 : 0;
        if (!isDisableBackground()) {
            Icon background = isHoveredOrFocused()
                    ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                    : Icon.TOOLBAR_BUTTON_BACKGROUND;
            background.getBlitter()
                    .dest(getX() - 1, getY() + yOffset, 18, 20)
                    .zOffset(2)
                    .blit(graphics);
        }

        if (kind == Kind.SPEED) {
            var font = Minecraft.getInstance().font;
            int textWidth = font.width(state);
            float scale = Math.min(1.0F, 13.0F / Math.max(1, textWidth));
            var pose = graphics.pose();
            pose.pushPose();
            // Draw after and above AE2's toolbar background, and shrink 128x/256x to fit.
            pose.translate(getX() + 8.0F, getY() + 8.0F + yOffset, 10.0F);
            pose.scale(scale, scale, 1.0F);
            // Center using the scaled font height as well as its width. Keeping the
            // origin at the button center avoids 128x/256x creeping toward the top.
            // Font glyphs sit one pixel above the nominal line box; compensate so the
            // visible multiplier, not merely its line box, is vertically centered.
            graphics.drawString(font, state, -textWidth / 2, -(font.lineHeight - 1) / 2, 0xFFFFFFFF, true);
            pose.popPose();
        } else {
            Icon icon = getIcon();
            if (icon != null) {
                icon.getBlitter()
                        .dest(getX(), getY() + 1 + yOffset)
                        .zOffset(3)
                        .blit(graphics);
            }
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        Component title = Component.translatable(kind == Kind.SPEED
                ? "gui.jdttimeaccelerators.ae2.speed"
                : "gui.jdttimeaccelerators.ae2.mode");
        Component hint = Component.translatable(kind == Kind.SPEED
                ? "gui.jdttimeaccelerators.ae2.speed_hint"
                : "gui.jdttimeaccelerators.ae2.mode_hint");
        return List.of(title.copy().append(": ").append(state), hint);
    }
}
