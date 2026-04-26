package com.uncraftbar.jdttimeaccelerators.client.screens;

import com.uncraftbar.jdttimeaccelerators.common.containers.TimeAcceleratorT1Container;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TimeAcceleratorT1Screen extends TimeAcceleratorScreen<TimeAcceleratorT1Container> {
    public TimeAcceleratorT1Screen(TimeAcceleratorT1Container container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
