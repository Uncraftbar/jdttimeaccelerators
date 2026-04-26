package com.uncraftbar.jdttimeaccelerators.setup;

import com.uncraftbar.jdttimeaccelerators.client.blockentityrenders.TimeAcceleratorT2BER;
import com.uncraftbar.jdttimeaccelerators.client.screens.TimeAcceleratorT1Screen;
import com.uncraftbar.jdttimeaccelerators.client.screens.TimeAcceleratorT2Screen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.TimeAcceleratorT1_Container.get(), TimeAcceleratorT1Screen::new);
        event.register(Registration.TimeAcceleratorT2_Container.get(), TimeAcceleratorT2Screen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.TimeAcceleratorT2BE.get(), TimeAcceleratorT2BER::new);
    }
}
