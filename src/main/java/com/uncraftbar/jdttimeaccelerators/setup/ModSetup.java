package com.uncraftbar.jdttimeaccelerators.setup;

import com.uncraftbar.jdttimeaccelerators.JDTTimeAccelerators;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSetup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JDTTimeAccelerators.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_JDT_TIME_ACCELERATORS = CREATIVE_MODE_TABS.register(JDTTimeAccelerators.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.jdttimeaccelerators"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(Registration.TimeAcceleratorT2_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept(Registration.TimeAcceleratorT1_ITEM.get());
                output.accept(Registration.TimeAcceleratorT2_ITEM.get());
            })
            .build());
}
