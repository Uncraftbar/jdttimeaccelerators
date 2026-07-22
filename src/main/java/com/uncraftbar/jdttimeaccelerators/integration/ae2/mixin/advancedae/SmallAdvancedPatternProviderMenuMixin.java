package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.advancedae;

import java.lang.reflect.Field;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.AE2AccelerationHost;
import com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.AEBaseMenuAccessor;

/**
 * SmallAdvPatternProviderMenu calls AdvancedAE's protected superclass constructor.
 * AppFlux configures its slot at that superclass tail after our superclass injection,
 * replacing the slot list. Re-attach JDTTA's independent slot at the concrete small
 * menu tail so both inventories are exposed, exactly as on the full-size provider.
 */
@Mixin(targets = "net.pedroksl.advanced_ae.gui.advpatternprovider.SmallAdvPatternProviderMenu", remap = false)
public abstract class SmallAdvancedPatternProviderMenuMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdtta$attachAccelerationSlot(CallbackInfo ci) {
        try {
            Class<?> type = this.getClass();
            Field field = null;
            while (type != null && field == null) {
                try {
                    field = type.getDeclaredField("logic");
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            if (field == null) throw new NoSuchFieldException("logic");
            field.setAccessible(true);
            var host = (AE2AccelerationHost) field.get(this);
            ((AEBaseMenuAccessor) this).jdtta$setupUpgrades(host.jdtta$getAccelerationUpgrades());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("AdvancedAE small provider logic field missing", e);
        }
    }
}
