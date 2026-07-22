package com.uncraftbar.jdttimeaccelerators.integration.ae2.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.client.gui.WidgetContainer;

@Mixin(value = AEBaseScreen.class, remap = false)
public interface AEBaseScreenAccessor {
    @Accessor("verticalToolbar") VerticalButtonBar jdtta$getVerticalToolbar();
    @Accessor("widgets") WidgetContainer jdtta$getWidgets();
}
