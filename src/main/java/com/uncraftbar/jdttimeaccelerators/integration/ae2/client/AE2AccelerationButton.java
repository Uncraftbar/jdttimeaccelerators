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
        PATTERN_MODE,
        INTERFACE_MODE,
        TARGET
    }

    private final Kind kind;
    private Component state = Component.empty();
    private Component tooltipState = Component.empty();
    private boolean conditional;

    public AE2AccelerationButton(Kind kind, OnPress onPress) {
        super(onPress);
        this.kind = kind;
    }

    public void setState(Component state, boolean conditional) {
        this.state = state;
        this.tooltipState = state;
        this.conditional = conditional;
    }

    public void setState(Component state, Component tooltipState, boolean conditional) {
        this.state = state;
        this.tooltipState = tooltipState;
        this.conditional = conditional;
    }

    @Override
    protected Icon getIcon() {
        if (kind == Kind.PATTERN_MODE) {
            return conditional ? Icon.CRAFT_HAMMER : Icon.REDSTONE_IGNORE;
        }
        if (kind == Kind.INTERFACE_MODE) {
            return conditional ? Icon.REDSTONE_ON : Icon.REDSTONE_IGNORE;
        }
        // Speed and target buttons draw their current value directly.
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

        if (kind == Kind.SPEED || kind == Kind.TARGET) {
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
        String titleKey = switch (kind) {
            case SPEED -> "gui.jdttimeaccelerators.ae2.speed";
            case TARGET -> "gui.jdttimeaccelerators.ae2.target";
            default -> "gui.jdttimeaccelerators.ae2.mode";
        };
        String hintKey = switch (kind) {
            case SPEED -> "gui.jdttimeaccelerators.ae2.speed_hint";
            case TARGET -> "gui.jdttimeaccelerators.ae2.target_hint";
            case PATTERN_MODE -> "gui.jdttimeaccelerators.ae2.pattern_mode_hint";
            case INTERFACE_MODE -> "gui.jdttimeaccelerators.ae2.interface_mode_hint";
        };
        Component title = Component.translatable(titleKey);
        Component hint = Component.translatable(hintKey);
        return List.of(title.copy().append(": ").append(tooltipState), hint);
    }
}
