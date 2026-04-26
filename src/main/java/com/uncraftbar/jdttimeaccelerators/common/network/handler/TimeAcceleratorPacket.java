package com.uncraftbar.jdttimeaccelerators.common.network.handler;

import com.uncraftbar.jdttimeaccelerators.common.blockentities.TimeAcceleratorT1BE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.uncraftbar.jdttimeaccelerators.common.network.data.TimeAcceleratorPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimeAcceleratorPacket {
    public static final TimeAcceleratorPacket INSTANCE = new TimeAcceleratorPacket();

    public static TimeAcceleratorPacket get() {
        return INSTANCE;
    }

    public void handle(final TimeAcceleratorPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            AbstractContainerMenu container = sender.containerMenu;

            if (container instanceof BaseMachineContainer baseMachineContainer && baseMachineContainer.baseMachineBE instanceof TimeAcceleratorT1BE acceleratorBE) {
                acceleratorBE.setSpeedLevel(payload.speedLevel());
            }
        });
    }
}
