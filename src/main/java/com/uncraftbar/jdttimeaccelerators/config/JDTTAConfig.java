package com.uncraftbar.jdttimeaccelerators.config;

import com.direwolf20.justdirethings.setup.Config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class JDTTAConfig {
    public static final ModConfigSpec SERVER_SPEC;

    private static final ModConfigSpec.IntValue MAX_ACCELERATION_MULTIPLIER;
    private static final ModConfigSpec.IntValue MAX_STACKS_PER_TARGET;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder
                .comment("Limits shared by standalone Time Accelerators and AE2 card hosts.")
                .translation("config.jdttimeaccelerators.acceleration")
                .push("acceleration");

        MAX_ACCELERATION_MULTIPLIER = builder
                .comment(
                        "Maximum multiplier available to JDT Time Accelerators.",
                        "0 follows Just Dire Things' Time Wand maximum.",
                        "Other values are capped by the Time Wand maximum and rounded down",
                        "to the nearest power of two.")
                .translation("config.jdttimeaccelerators.max_multiplier")
                .defineInRange("maxMultiplier", 0, 0, 1 << 30);

        MAX_STACKS_PER_TARGET = builder
                .comment(
                        "How many JDT Time Accelerator machines and AE2 cards may accelerate",
                        "the same target during one server tick. 1 disables stacked acceleration.")
                .translation("config.jdttimeaccelerators.max_stacks_per_target")
                .defineInRange("maxStacksPerTarget", 1, 1, 64);

        builder.pop();
        SERVER_SPEC = builder.build();
    }

    private JDTTAConfig() {}

    public static int maxAccelerationMultiplier() {
        int wandMaximum = Math.max(1, Config.TIME_WAND_MAX_MULTIPLIER.get());
        int configuredMaximum = MAX_ACCELERATION_MULTIPLIER.get();
        int cappedMaximum = configuredMaximum == 0
                ? wandMaximum
                : Math.min(wandMaximum, configuredMaximum);
        return highestPowerOfTwoAtMost(cappedMaximum);
    }

    public static int maxStacksPerTarget() {
        return MAX_STACKS_PER_TARGET.get();
    }

    public static int highestPowerOfTwoAtMost(int value) {
        if (value <= 1) return 1;
        return Integer.highestOneBit(value);
    }
}
