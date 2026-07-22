package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Kept free of AE2 types so the base mod remains loadable without AE2. */
public final class AE2IntegrationBootstrap {
    private AE2IntegrationBootstrap() {}

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("ae2")) return;
        event.enqueueWork(() -> {
            try {
                Class.forName("com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2Integration")
                        .getMethod("initialize").invoke(null);
            } catch (ReflectiveOperationException | LinkageError e) {
                throw new IllegalStateException("Could not initialize optional AE2 integration", e);
            }
        });
    }
}
