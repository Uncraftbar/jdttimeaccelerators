package com.uncraftbar.jdttimeaccelerators.integration.ae2;

public interface AE2AccelerationMenu {
    int jdtta$getSyncedSpeedLevel();
    boolean jdtta$getSyncedConditional();
    boolean jdtta$getSyncedCardInstalled();
    int jdtta$getSyncedTargetMask();
    boolean jdtta$getSyncedTargetConfigurable();
    void jdtta$cycleSpeed(boolean backwards);
    void jdtta$toggleConditional();
    void jdtta$setTargetMask(int mask);
    net.minecraft.world.level.block.entity.BlockEntity jdtta$getHostBlockEntity();
}
