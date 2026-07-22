package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import net.neoforged.fml.loading.LoadingModList;

/** Prevents every AE2-targeting mixin from being considered when AE2 is absent. */
public final class AE2MixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (LoadingModList.get().getModFileById("ae2") == null) return false;
        if (mixinClassName.contains(".advancedae.")) {
            return LoadingModList.get().getModFileById("advanced_ae") != null;
        }
        if (mixinClassName.contains(".extendedae.")) {
            return LoadingModList.get().getModFileById("extendedae") != null;
        }
        return true;
    }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
