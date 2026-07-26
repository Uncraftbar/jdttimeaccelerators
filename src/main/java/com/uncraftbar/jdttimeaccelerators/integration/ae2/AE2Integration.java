package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Registers the card with AE2 and optional provider implementations found at runtime. */
public final class AE2Integration {
    private AE2Integration() {}

    public static void initialize() {
        var card = Registration.AE2_TIME_ACCELERATION_CARD.get();
        Upgrades.add(card, AEBlocks.PATTERN_PROVIDER, 1, "jdttimeaccelerators.ae2.pattern_providers");
        Upgrades.add(card, AEParts.PATTERN_PROVIDER, 1, "jdttimeaccelerators.ae2.pattern_providers");
        Upgrades.add(card, AEBlocks.INTERFACE, 1, "jdttimeaccelerators.ae2.interfaces");
        Upgrades.add(card, AEParts.INTERFACE, 1, "jdttimeaccelerators.ae2.interfaces");

        // Addon machines must be associated explicitly too. This is the intended AE2
        // API: the same association controls slot acceptance and the card's generated
        // compatibility tooltip. Register during common setup, before tooltips are cached.
        addOptional(card, "expandedae", "exp_pattern_provider", "jdttimeaccelerators.ae2.expandedae_pattern_providers");
        addOptional(card, "expandedae", "exp_pattern_provider_part", "jdttimeaccelerators.ae2.expandedae_pattern_providers");

        // ExtendedAE and MEGA Cells both reuse AE2's PatternProviderLogic/InterfaceLogic.
        addOptional(card, "extendedae", "ex_pattern_provider", "jdttimeaccelerators.ae2.extendedae_pattern_providers");
        addOptional(card, "extendedae", "ex_pattern_provider_part", "jdttimeaccelerators.ae2.extendedae_pattern_providers");
        addOptional(card, "extendedae", "ex_interface", "jdttimeaccelerators.ae2.extendedae_interfaces");
        addOptional(card, "extendedae", "ex_interface_part", "jdttimeaccelerators.ae2.extendedae_interfaces");
        addOptional(card, "extendedae", "oversize_interface", "jdttimeaccelerators.ae2.extendedae_interfaces");
        addOptional(card, "extendedae", "oversize_interface_part", "jdttimeaccelerators.ae2.extendedae_interfaces");
        addOptional(card, "megacells", "mega_pattern_provider", "jdttimeaccelerators.ae2.megacells_pattern_providers");
        addOptional(card, "megacells", "cable_mega_pattern_provider", "jdttimeaccelerators.ae2.megacells_pattern_providers");
        addOptional(card, "megacells", "mega_interface", "jdttimeaccelerators.ae2.megacells_interfaces");
        addOptional(card, "megacells", "cable_mega_interface", "jdttimeaccelerators.ae2.megacells_interfaces");

        // AdvancedAE has parallel logic; optional mixins bridge it to the same engine.
        addOptional(card, "advanced_ae", "adv_pattern_provider", "jdttimeaccelerators.ae2.advancedae_pattern_providers");
        addOptional(card, "advanced_ae", "adv_pattern_provider_part", "jdttimeaccelerators.ae2.advancedae_pattern_providers");
        addOptional(card, "advanced_ae", "small_adv_pattern_provider", "jdttimeaccelerators.ae2.advancedae_pattern_providers");
        addOptional(card, "advanced_ae", "small_adv_pattern_provider_part", "jdttimeaccelerators.ae2.advancedae_pattern_providers");
    }

    private static void addOptional(Item card, String namespace, String path, String group) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.ITEM.containsKey(id)) return;
        Item machine = BuiltInRegistries.ITEM.get(id);
        if (machine != null) Upgrades.add(card, machine, 1, group);
    }

}
