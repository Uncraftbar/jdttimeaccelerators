package com.uncraftbar.jdttimeaccelerators.client.screens;

import com.uncraftbar.jdttimeaccelerators.common.containers.TimeAcceleratorT2Container;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TimeAcceleratorT2Screen extends TimeAcceleratorScreen<TimeAcceleratorT2Container> {
    public TimeAcceleratorT2Screen(TimeAcceleratorT2Container container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
