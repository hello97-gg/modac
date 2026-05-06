package com.modmc.anticheat.check;

/**
 * Enum of all check types in the anticheat.
 * Each type maps to a config key and a display name.
 */
public enum CheckType {

    // Movement
    SPEED("speed", "Speed", "Movement"),
    FLY("fly", "Fly", "Movement"),
    NOFALL("nofall", "NoFall", "Movement"),
    JESUS("jesus", "Jesus", "Movement"),
    ELYTRA("elytra", "ElytraFly", "Movement"),
    NOSLOW("noslow", "NoSlow", "Movement"),

    // Combat
    REACH("reach", "Reach", "Combat"),
    KILLAURA("killaura", "KillAura", "Combat"),
    AUTOCLICKER("autoclicker", "AutoClicker", "Combat"),
    VELOCITY("velocity", "Velocity", "Combat"),
    CRITICALS("criticals", "Criticals", "Combat"),

    // Player
    TIMER("timer", "Timer", "Player"),
    BADPACKETS("badpackets", "BadPackets", "Player"),
    INVENTORY("inventory", "Inventory", "Player"),
    SCAFFOLD("scaffold", "Scaffold", "Player"),
    FASTBREAK("fastbreak", "FastBreak", "Player");

    private final String configKey;
    private final String displayName;
    private final String category;

    CheckType(String configKey, String displayName, String category) {
        this.configKey = configKey;
        this.displayName = displayName;
        this.category = category;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Get a CheckType by its config key.
     */
    public static CheckType fromConfigKey(String key) {
        for (CheckType type : values()) {
            if (type.configKey.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}
