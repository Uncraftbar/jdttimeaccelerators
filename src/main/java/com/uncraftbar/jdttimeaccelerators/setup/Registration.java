package com.uncraftbar.jdttimeaccelerators.setup;

import com.uncraftbar.jdttimeaccelerators.JDTTimeAccelerators;
import com.uncraftbar.jdttimeaccelerators.common.blockentities.TimeAcceleratorT1BE;
import com.uncraftbar.jdttimeaccelerators.common.blockentities.TimeAcceleratorT2BE;
import com.uncraftbar.jdttimeaccelerators.common.blocks.TimeAcceleratorT1;
import com.uncraftbar.jdttimeaccelerators.common.blocks.TimeAcceleratorT2;
import com.uncraftbar.jdttimeaccelerators.common.containers.TimeAcceleratorT1Container;
import com.uncraftbar.jdttimeaccelerators.common.containers.TimeAcceleratorT2Container;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JDTTimeAccelerators.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JDTTimeAccelerators.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JDTTimeAccelerators.MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, JDTTimeAccelerators.MODID);

    public static final DeferredHolder<Block, TimeAcceleratorT1> TimeAcceleratorT1 = BLOCKS.registerBlock("timeacceleratort1", TimeAcceleratorT1::new, Registration::machineProperties);
    public static final DeferredHolder<Item, BlockItem> TimeAcceleratorT1_ITEM = ITEMS.registerSimpleBlockItem("timeacceleratort1", TimeAcceleratorT1);
    public static final DeferredHolder<Block, TimeAcceleratorT2> TimeAcceleratorT2 = BLOCKS.registerBlock("timeacceleratort2", TimeAcceleratorT2::new, Registration::machineProperties);
    public static final DeferredHolder<Item, BlockItem> TimeAcceleratorT2_ITEM = ITEMS.registerSimpleBlockItem("timeacceleratort2", TimeAcceleratorT2);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimeAcceleratorT1BE>> TimeAcceleratorT1BE = BLOCK_ENTITIES.register("timeacceleratort1", () -> new BlockEntityType<>(TimeAcceleratorT1BE::new, TimeAcceleratorT1.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimeAcceleratorT2BE>> TimeAcceleratorT2BE = BLOCK_ENTITIES.register("timeacceleratort2", () -> new BlockEntityType<>(TimeAcceleratorT2BE::new, TimeAcceleratorT2.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<TimeAcceleratorT1Container>> TimeAcceleratorT1_Container = CONTAINERS.register("timeacceleratort1_container", () -> IMenuTypeExtension.create(TimeAcceleratorT1Container::new));
    public static final DeferredHolder<MenuType<?>, MenuType<TimeAcceleratorT2Container>> TimeAcceleratorT2_Container = CONTAINERS.register("timeacceleratort2_container", () -> IMenuTypeExtension.create(TimeAcceleratorT2Container::new));

    private static Block.Properties machineProperties() {
        return Block.Properties.of()
                .sound(net.minecraft.world.level.block.SoundType.METAL)
                .strength(2.0f)
                .isRedstoneConductor(com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock::never);
    }

    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CONTAINERS.register(eventBus);
    }
}
