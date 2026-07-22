package com.uncraftbar.jdttimeaccelerators.integration.ae2;

import java.util.Set;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;

/** Internal bridge implemented on AE2 logic objects by optional mixins. */
public interface AE2AccelerationHost {
    IManagedGridNode jdtta$getMainNode();
    IActionSource jdtta$getActionSource();
    IUpgradeInventory jdtta$getAccelerationUpgrades();
    /** Upgrade inventory supplied by another integration, such as AppliedFlux. */
    default @Nullable IUpgradeInventory jdtta$getExternalUpgrades() {
        return this instanceof IUpgradeableObject upgradeable ? upgradeable.getUpgrades() : null;
    }
    BlockEntity jdtta$getHostBlockEntity();
    Set<Direction> jdtta$getTargetDirections();
    int jdtta$getSpeedLevel();
    void jdtta$setSpeedLevel(int level);
    boolean jdtta$isConditional();
    void jdtta$setConditional(boolean conditional);
    int jdtta$getFluidRemainder();
    void jdtta$setFluidRemainder(int remainder);
    default boolean jdtta$isPatternProvider() { return false; }
    @Nullable BlockPos jdtta$getRequestedTarget();
    void jdtta$setRequestedTarget(@Nullable BlockPos pos, long expiresAt);
    long jdtta$getRequestedTargetExpiry();
    int jdtta$getRequestedTargetIdleTicks();
    void jdtta$setRequestedTargetIdleTicks(int ticks);
    void jdtta$saveChanges();
}
