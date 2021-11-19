package bletch.common;

import bletch.common.core.CommonEntities;

public enum MovementMode {
    WALK((byte) 1, 1.0F, CommonEntities.ANIMATION_VILLAGER_WALK),
    RUN((byte) 2, 1.4F, CommonEntities.ANIMATION_VILLAGER_RUN),
    CREEP((byte) 3, 0.8F, CommonEntities.ANIMATION_VILLAGER_CREEP);

    public byte id;
    public float speedMultiplier;
    public String animation;

    MovementMode(byte id, float speedMultiplier, String animation) {
        this.id = id;
        this.speedMultiplier = speedMultiplier;
        this.animation = animation;
    }

    public static MovementMode valueOf(byte id) {
        for (MovementMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }

        return null;
    }
}
