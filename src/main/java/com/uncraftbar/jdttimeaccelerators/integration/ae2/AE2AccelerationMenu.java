package com.uncraftbar.jdttimeaccelerators.integration.ae2;

public interface AE2AccelerationMenu {
    int jdtta$getSyncedSpeedLevel();
    boolean jdtta$getSyncedConditional();
    boolean jdtta$getSyncedCardInstalled();
    void jdtta$cycleSpeed(boolean backwards);
    void jdtta$toggleConditional();
}
