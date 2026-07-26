package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.Direction;

/** Six-bit target-side mask used by configurable full-block hosts. */
public final class AE2AccelerationTarget {
    public static final int ALL_MASK = (1 << Direction.values().length) - 1;

    private AE2AccelerationTarget() {}

    public static int bit(Direction direction) {
        return 1 << direction.ordinal();
    }

    public static boolean contains(int mask, Direction direction) {
        return (sanitize(mask) & bit(direction)) != 0;
    }

    public static int sanitize(int mask) {
        return mask & ALL_MASK;
    }

    public static Set<Direction> directions(int mask) {
        var result = EnumSet.noneOf(Direction.class);
        int sanitized = sanitize(mask);
        for (Direction direction : Direction.values()) {
            if ((sanitized & bit(direction)) != 0) result.add(direction);
        }
        return result;
    }

    public static int count(int mask) {
        return Integer.bitCount(sanitize(mask));
    }
}
