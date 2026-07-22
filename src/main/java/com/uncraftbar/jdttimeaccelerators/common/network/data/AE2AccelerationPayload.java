package com.uncraftbar.jdttimeaccelerators.common.network.data;

import com.uncraftbar.jdttimeaccelerators.JDTTimeAccelerators;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** GUI action for the optional AE2 acceleration controls. */
public record AE2AccelerationPayload(int action, boolean backwards) implements CustomPacketPayload {
    public static final int CYCLE_SPEED = 0;
    public static final int TOGGLE_CONDITIONAL = 1;
    public static final Type<AE2AccelerationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTTimeAccelerators.MODID, "ae2_acceleration_config"));
    public static final StreamCodec<FriendlyByteBuf, AE2AccelerationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AE2AccelerationPayload::action,
            ByteBufCodecs.BOOL, AE2AccelerationPayload::backwards,
            AE2AccelerationPayload::new);

    @Override public Type<AE2AccelerationPayload> type() { return TYPE; }
}
