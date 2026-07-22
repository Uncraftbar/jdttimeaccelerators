package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.helpers.InterfaceLogic;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationEngine;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;

@Mixin(targets = "appeng.helpers.InterfaceLogic$Ticker", remap = false)
public abstract class InterfaceTickerMixin {
    @Shadow @Final InterfaceLogic this$0;

    @Inject(method = "getTickingRequest", at = @At("HEAD"), cancellable = true)
    private void jdtta$requestEveryTick(IGridNode node, CallbackInfoReturnable<TickingRequest> cir) {
        var host = (AE2AccelerationHost) (Object) this$0;
        // AE2 snapshots this request when the node joins the grid; it does not ask
        // again when an upgrade is inserted. Always register a one-tick capable
        // tracker, but let it start asleep until the card callback alerts the node.
        // This makes every selected multiplier run once per server tick instead of
        // sporadically at AE2's stock interface/provider interval.
        cir.setReturnValue(new TickingRequest(1, 1,
                !AE2AccelerationEngine.isCardInstalled(host)));
    }

    @Inject(method = "tickingRequest", at = @At("HEAD"))
    private void jdtta$accelerate(IGridNode node, int ticks, CallbackInfoReturnable<TickRateModulation> cir) {
        var host = (AE2AccelerationHost) (Object) this$0;
        if (AE2AccelerationEngine.isCardInstalled(host)) {
            AE2AccelerationEngine.tick(host);
        }
    }

    @Inject(method = "tickingRequest", at = @At("RETURN"), cancellable = true)
    private void jdtta$keepAwake(IGridNode node, int ticks, CallbackInfoReturnable<TickRateModulation> cir) {
        var host = (AE2AccelerationHost) (Object) this$0;
        if (AE2AccelerationEngine.isCardInstalled(host)) {
            // AE2's stock-management work has already run; only override sleep modulation.
            cir.setReturnValue(TickRateModulation.URGENT);
        }
    }
}
