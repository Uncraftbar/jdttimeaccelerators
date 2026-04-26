package com.uncraftbar.jdttimeaccelerators;

import com.uncraftbar.jdttimeaccelerators.common.blockentities.TimeAcceleratorT1BE;
import com.uncraftbar.jdttimeaccelerators.common.network.PacketHandler;
import com.uncraftbar.jdttimeaccelerators.setup.ClientSetup;
import com.uncraftbar.jdttimeaccelerators.setup.ModSetup;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(JDTTimeAccelerators.MODID)
public class JDTTimeAccelerators {
    public static final String MODID = "jdttimeaccelerators";

    public JDTTimeAccelerators(IEventBus modEventBus, ModContainer container) {
        Registration.init(modEventBus);
        ModSetup.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(PacketHandler::registerNetworking);
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(ClientSetup::registerScreens);
            modEventBus.addListener(ClientSetup::registerRenderers);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof TimeAcceleratorT1BE accelerator ? accelerator.getEnergyStorage() : null,
                Registration.TimeAcceleratorT1.get(), Registration.TimeAcceleratorT2.get());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof TimeAcceleratorT1BE accelerator ? accelerator.getFluidTank() : null,
                Registration.TimeAcceleratorT1.get(), Registration.TimeAcceleratorT2.get());
    }
}
