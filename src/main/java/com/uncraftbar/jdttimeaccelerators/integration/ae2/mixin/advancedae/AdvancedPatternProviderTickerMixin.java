package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.advancedae;

import java.lang.reflect.Field;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;

@Mixin(targets = "net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic$Ticker", remap = false)
public abstract class AdvancedPatternProviderTickerMixin {
    @Unique private AE2AccelerationHost jdtta$host() {
        try {
            Field field = this.getClass().getDeclaredField("this$0");
            field.setAccessible(true);
            return (AE2AccelerationHost) field.get(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("AdvancedAE ticker owner field missing", e);
        }
    }

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void jdtta$requestEveryTick(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        cir.setReturnValue(new TickingRequest(1, 20, !AE2AccelerationEngine.isCardInstalled(jdtta$host())));
    }

    @Inject(method = "tickingRequest", at = @At("HEAD"))
    private void jdtta$accelerate(IGridNode node, int ticks, CallbackInfoReturnable<TickRateModulation> cir) {
        AE2AccelerationHost host = jdtta$host();
        if (AE2AccelerationEngine.isCardInstalled(host)) AE2AccelerationEngine.tick(host);
    }

    @Inject(method = "tickingRequest", at = @At("RETURN"), cancellable = true)
    private void jdtta$keepAwake(IGridNode node, int ticks, CallbackInfoReturnable<TickRateModulation> cir) {
        if (AE2AccelerationEngine.shouldTickUrgently(jdtta$host())) {
            cir.setReturnValue(TickRateModulation.URGENT);
        }
    }
}
