package com.uncraftbar.jdttimeaccelerators.integration.ae2.client;

import java.util.List;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** One independently selectable adjacent side in the full-block target screen. */
final class AE2TargetSideButton extends IconButton {
    private final Direction direction;
    private final ItemStack displayStack;
    private final boolean valid;
    private boolean selected;

    AE2TargetSideButton(Direction direction, ItemStack displayStack, boolean selected,
            boolean valid, OnPress onPress) {
        super(onPress);
        this.direction = direction;
        this.displayStack = displayStack;
        this.selected = selected;
        this.valid = valid;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected Icon getIcon() {
        return null;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        int hoverOffset = isHoveredOrFocused() ? 1 : 0;
        Icon background = selected
                ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS
                : isHoveredOrFocused()
                        ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                        : Icon.TOOLBAR_BUTTON_BACKGROUND;
        background.getBlitter()
                .dest(getX() - 1, getY() + hoverOffset, 18, 20)
                .zOffset(2)
                .blit(graphics);
        if (!displayStack.isEmpty()) {
            graphics.renderItem(displayStack, getX(), getY() + 1 + hoverOffset);
        }
        if (selected && !valid) {
            Icon.INVALID.getBlitter()
                    .dest(getX() + 8, getY() + 9 + hoverOffset, 8, 8)
                    .zOffset(10)
                    .blit(graphics);
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        Component side = Component.translatable("gui.jdttimeaccelerators.ae2.side." + direction.getName());
        Component state = Component.translatable(selected
                ? "gui.jdttimeaccelerators.ae2.side.selected"
                : "gui.jdttimeaccelerators.ae2.side.excluded");
        if (selected && !valid) {
            return List.of(side, state,
                    Component.translatable("gui.jdttimeaccelerators.ae2.side.invalid"));
        }
        return List.of(side, state);
    }
}
