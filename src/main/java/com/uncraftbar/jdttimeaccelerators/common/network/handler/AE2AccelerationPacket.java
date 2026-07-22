package com.uncraftbar.jdttimeaccelerators.common.network.handler;

import com.uncraftbar.jdttimeaccelerators.common.network.data.AE2AccelerationPayload;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationMenu;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AE2AccelerationPacket {
    private AE2AccelerationPacket() {}

    public static void handle(AE2AccelerationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof AE2AccelerationMenu menu)
                    || !menu.jdtta$getSyncedCardInstalled()) {
                return;
            }
            switch (payload.action()) {
                case AE2AccelerationPayload.CYCLE_SPEED -> menu.jdtta$cycleSpeed(payload.backwards());
                case AE2AccelerationPayload.TOGGLE_CONDITIONAL -> menu.jdtta$toggleConditional();
                default -> { }
            }
        });
    }
}
