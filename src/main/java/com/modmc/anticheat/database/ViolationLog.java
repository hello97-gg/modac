package com.modmc.anticheat.database;

/**
 * Represents a violation log entry from the database.
 */
public class ViolationLog {

    private final int id;
    private final String uuid;
    private final String playerName;
    private final String checkType;
    private final int vl;
    private final String details;
    private final String timestamp;

    public ViolationLog(int id, String uuid, String playerName, String checkType,
                        int vl, String details, String timestamp) {
        this.id = id;
        this.uuid = uuid;
        this.playerName = playerName;
        this.checkType = checkType;
        this.vl = vl;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public String getCheckType() { return checkType; }
    public int getVl() { return vl; }
    public String getDetails() { return details; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + playerName + " | " + checkType + " VL:" + vl + " | " + details;
    }
}
