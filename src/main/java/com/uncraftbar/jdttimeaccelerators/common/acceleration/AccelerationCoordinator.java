package com.uncraftbar.jdttimeaccelerators.common.acceleration;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.uncraftbar.jdttimeaccelerators.config.JDTTAConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Enforces the configured per-target acceleration stack limit across standalone
 * machines and AE2 card hosts.
 */
public final class AccelerationCoordinator {
    private static final Map<ServerLevel, TickClaims> CLAIMED_TARGETS = new WeakHashMap<>();

    private AccelerationCoordinator() {}

    public static boolean tryClaim(ServerLevel level, BlockPos targetPos) {
        synchronized (CLAIMED_TARGETS) {
            TickClaims claims = claimsForCurrentTick(level);
            long packedPos = targetPos.asLong();
            int currentClaims = claims.positions.getOrDefault(packedPos, 0);
            if (currentClaims >= JDTTAConfig.maxStacksPerTarget()) return false;
            claims.positions.put(packedPos, currentClaims + 1);
            return true;
        }
    }

    public static void release(ServerLevel level, BlockPos targetPos) {
        synchronized (CLAIMED_TARGETS) {
            TickClaims claims = CLAIMED_TARGETS.get(level);
            if (claims == null || claims.gameTime != level.getGameTime()) return;

            long packedPos = targetPos.asLong();
            int currentClaims = claims.positions.getOrDefault(packedPos, 0);
            if (currentClaims <= 1) {
                claims.positions.remove(packedPos);
            } else {
                claims.positions.put(packedPos, currentClaims - 1);
            }
        }
    }

    private static TickClaims claimsForCurrentTick(ServerLevel level) {
        TickClaims claims = CLAIMED_TARGETS.computeIfAbsent(level, ignored -> new TickClaims());
        long gameTime = level.getGameTime();
        if (claims.gameTime != gameTime) {
            claims.gameTime = gameTime;
            claims.positions.clear();
        }
        return claims;
    }

    private static final class TickClaims {
        private long gameTime = Long.MIN_VALUE;
        private final Map<Long, Integer> positions = new HashMap<>();
    }
}
