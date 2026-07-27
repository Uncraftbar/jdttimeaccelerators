package com.uncraftbar.jdttimeaccelerators.integration.ae2.client;

import java.util.EnumMap;
import java.util.Map;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.AE2Button;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import com.direwolf20.justdirethings.datagen.JustDireBlockTags;
import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationTarget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * AE2-native sub-screen for independently selecting any combination of adjacent
 * targets. This deliberately uses no addon implementation classes or assets.
 */
public final class AE2TargetSideScreen<C extends AEBaseMenu, P extends AEBaseScreen<C>>
        extends AESubScreen<C, P> {
    private final AE2AccelerationMenu accelerationMenu;
    private final Map<Direction, AE2TargetSideButton> sideButtons = new EnumMap<>(Direction.class);
    private int targetMask;
    private boolean acceptedIngredientsOnly;
    private AE2Button targetModeButton;

    private AE2TargetSideScreen(P parent, AE2AccelerationMenu accelerationMenu) {
        super(parent, "/screens/jdtta_target_sides.json");
        this.accelerationMenu = accelerationMenu;
        this.targetMask = AE2AccelerationTarget.sanitize(accelerationMenu.jdtta$getSyncedTargetMask());
        this.acceptedIngredientsOnly =
                accelerationMenu.jdtta$getSyncedAcceptedIngredientsOnly();

        widgets.add("return", new TabButton(Icon.BACK,
                Component.translatable("gui.jdttimeaccelerators.ae2.target"), button -> returnToParent()));
        var selectAll = widgets.addButton("selectAll",
                Component.translatable("gui.jdttimeaccelerators.ae2.select_all"),
                () -> setMask(AE2AccelerationTarget.ALL_MASK));
        selectAll.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("gui.jdttimeaccelerators.ae2.select_all_warning")));
        widgets.addButton("clear",
                Component.translatable("gui.jdttimeaccelerators.ae2.clear"),
                () -> setMask(0));
        if (accelerationMenu.jdtta$isPatternProviderMenu()) {
            targetModeButton = widgets.addButton("targetMode",
                    targetModeLabel(), this::toggleTargetMode);
            updateTargetModeButton();
        }

        BlockEntity host = accelerationMenu.jdtta$getHostBlockEntity();
        BlockOrientation orientation = host instanceof AEBaseBlockEntity aeHost
                ? aeHost.getOrientation()
                : BlockOrientation.get(host.getBlockState());
        for (RelativeSide relativeSide : RelativeSide.values()) {
            Direction direction = orientation.getSide(relativeSide);
            AE2TargetSideButton button = new AE2TargetSideButton(direction,
                    getDisplayStack(host, direction),
                    AE2AccelerationTarget.contains(targetMask, direction),
                    isEligibleTarget(host, direction),
                    ignored -> toggle(direction));
            sideButtons.put(direction, button);
            widgets.add(relativeSide.name().toLowerCase(java.util.Locale.ROOT), button);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void open(AEBaseScreen<?> parent, AE2AccelerationMenu accelerationMenu) {
        ((AEBaseScreen) parent).switchToScreen(new AE2TargetSideScreen(
                (AEBaseScreen) parent, accelerationMenu));
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void toggle(Direction direction) {
        int bit = AE2AccelerationTarget.bit(direction);
        setMask((targetMask & bit) != 0 ? targetMask & ~bit : targetMask | bit);
    }

    private void setMask(int mask) {
        targetMask = AE2AccelerationTarget.sanitize(mask);
        sideButtons.forEach((direction, button) ->
                button.setSelected(AE2AccelerationTarget.contains(targetMask, direction)));
        PacketDistributor.sendToServer(new AE2AccelerationPayload(
                AE2AccelerationPayload.SET_TARGET_MASK, false, targetMask));
    }

    private void toggleTargetMode() {
        acceptedIngredientsOnly = !acceptedIngredientsOnly;
        updateTargetModeButton();
        PacketDistributor.sendToServer(new AE2AccelerationPayload(
                AE2AccelerationPayload.TOGGLE_ACCEPTED_INGREDIENTS_ONLY, false));
    }

    private void updateTargetModeButton() {
        if (targetModeButton == null) return;
        targetModeButton.setMessage(targetModeLabel());
        targetModeButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable(acceptedIngredientsOnly
                        ? "gui.jdttimeaccelerators.ae2.accepted_only_hint"
                        : "gui.jdttimeaccelerators.ae2.selected_sides_hint")));
    }

    private Component targetModeLabel() {
        return Component.translatable(acceptedIngredientsOnly
                ? "gui.jdttimeaccelerators.ae2.accepted_only"
                : "gui.jdttimeaccelerators.ae2.selected_sides");
    }

    private static ItemStack getDisplayStack(BlockEntity host, Direction direction) {
        Level level = host.getLevel();
        if (level == null) return ItemStack.EMPTY;
        BlockEntity adjacent = level.getBlockEntity(host.getBlockPos().relative(direction));
        if (adjacent instanceof CableBusBlockEntity cableBus) {
            var part = cableBus.getPart(direction.getOpposite());
            if (part != null) return new ItemStack(part.getPartItem());
        }
        return new ItemStack(level.getBlockState(host.getBlockPos().relative(direction)).getBlock());
    }

    private static boolean isEligibleTarget(BlockEntity host, Direction direction) {
        Level level = host.getLevel();
        if (level == null) return false;
        var pos = host.getBlockPos().relative(direction);
        var state = level.getBlockState(pos);
        BlockEntity target = level.getBlockEntity(pos);
        boolean tickingBlockEntity = target != null
                && state.getTicker(level, target.getType()) != null;
        return !state.is(JustDireBlockTags.TICK_SPEED_DENY)
                && (state.isRandomlyTicking() || tickingBlockEntity);
    }
}
