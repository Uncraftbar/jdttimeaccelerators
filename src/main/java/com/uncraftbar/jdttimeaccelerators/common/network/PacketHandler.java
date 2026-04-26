package com.uncraftbar.jdttimeaccelerators.common.network;

import com.uncraftbar.jdttimeaccelerators.JDTTimeAccelerators;
import com.uncraftbar.jdttimeaccelerators.common.network.data.TimeAcceleratorPayload;
import com.uncraftbar.jdttimeaccelerators.common.network.handler.TimeAcceleratorPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(JDTTimeAccelerators.MODID);
        registrar.playToServer(TimeAcceleratorPayload.TYPE, TimeAcceleratorPayload.STREAM_CODEC, TimeAcceleratorPacket.get()::handle);
    }
}
