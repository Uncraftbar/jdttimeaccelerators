package com.uncraftbar.jdttimeaccelerators.common.network.data;

import com.uncraftbar.jdttimeaccelerators.JDTTimeAccelerators;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimeAcceleratorPayload(
        int speedLevel
) implements CustomPacketPayload {
    public static final Type<TimeAcceleratorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTTimeAccelerators.MODID, "time_accelerator_packet"));

    @Override
    public Type<TimeAcceleratorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, TimeAcceleratorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TimeAcceleratorPayload::speedLevel,
            TimeAcceleratorPayload::new
    );
}
