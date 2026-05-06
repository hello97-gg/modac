package com.modmc.anticheat.data;

import com.modmc.anticheat.check.CheckType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player data tracking object.
 * Stores movement history, click data, violation levels, and state flags.
 */
public class PlayerData {

    private final UUID uuid;
    private final String name;
    private Player player;

    // --- Movement Tracking ---
    private final LinkedList<Location> locationHistory = new LinkedList<>();
    private Location lastLocation;
    private Location lastGroundLocation;
    private double deltaX, deltaY, deltaZ;
    private double lastDeltaX, lastDeltaY, lastDeltaZ;
    private float deltaYaw, deltaPitch;
    private float lastDeltaYaw, lastDeltaPitch;
    private boolean onGround;
    private boolean lastOnGround;
    private boolean clientOnGround; // What the client claims
    private int airTicks;
    private int groundTicks;
    private long lastMovementTime;

    // --- Combat Tracking ---
    private final LinkedList<Long> clickTimestamps = new LinkedList<>();
    private long lastAttackTime;
    private int attackTick;
    private UUID lastTarget;

    // --- Velocity Tracking ---
    private long lastVelocityTime;
    private double lastVelocityX, lastVelocityY, lastVelocityZ;
    private boolean velocityPending;
    private int velocityTicks;

    // --- Violation Tracking ---
    private final Map<CheckType, Integer> violations = new ConcurrentHashMap<>();

    // --- State Flags ---
    private boolean alertsEnabled = true;
    private boolean verboseEnabled = false;
    private long joinTime;
    private int totalPackets;
    private long lastPacketTime;
    private boolean teleporting;
    private int teleportTicks;

    // --- Inventory ---
    private boolean inventoryOpen;
    private long lastInventoryClose;

    // --- Scaffold ---
    private long lastBlockPlace;
    private int blocksPlaced;
    private int scaffoldSignalCount;
    private boolean placingBelow;
    private float lastPlacePitch;
    private long lastScaffoldCancelTime;
    private final List<Long> placementIntervals = new ArrayList<>();

    // --- Digging ---
    private boolean digging;
    private long lastDigStartTime;
    private long digStartTime;
    private int digBlockX, digBlockY, digBlockZ;

    // --- Timer ---
    private long timerLastCheck;
    private int timerPacketCount;
    private int timerCancelTicks;

    // Max history size
    private static final int MAX_LOCATION_HISTORY = 20;
    private static final int MAX_CLICK_HISTORY = 100;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.player = player;
        this.joinTime = System.currentTimeMillis();
        this.lastLocation = player.getLocation();
        this.lastMovementTime = System.currentTimeMillis();

        // Initialize all violation levels to 0
        for (CheckType type : CheckType.values()) {
            violations.put(type, 0);
        }
    }

    // ===========================
    // Movement Methods
    // ===========================

    public void updateLocation(Location newLocation, boolean clientGround) {
        if (lastLocation != null) {
            lastDeltaX = deltaX;
            lastDeltaY = deltaY;
            lastDeltaZ = deltaZ;

            deltaX = newLocation.getX() - lastLocation.getX();
            deltaY = newLocation.getY() - lastLocation.getY();
            deltaZ = newLocation.getZ() - lastLocation.getZ();

            lastDeltaYaw = deltaYaw;
            lastDeltaPitch = deltaPitch;
            deltaYaw = newLocation.getYaw() - lastLocation.getYaw();
            deltaPitch = newLocation.getPitch() - lastLocation.getPitch();
        }

        lastOnGround = onGround;
        clientOnGround = clientGround;

        // Add to history
        locationHistory.addFirst(newLocation.clone());
        if (locationHistory.size() > MAX_LOCATION_HISTORY) {
            locationHistory.removeLast();
        }

        // Update ground state
        if (clientGround || com.modmc.anticheat.util.PlayerUtil.isInLiquid(player) || com.modmc.anticheat.util.PlayerUtil.isClimbing(player)) {
            airTicks = 0;
            groundTicks++;
            if (clientGround) {
                lastGroundLocation = newLocation.clone();
            }
        } else {
            airTicks++;
            groundTicks = 0;
        }

        // Handle teleport cooldown
        if (teleportTicks > 0) {
            teleportTicks--;
            if (teleportTicks == 0) {
                teleporting = false;
            }
        }

        lastLocation = newLocation.clone();
        lastMovementTime = System.currentTimeMillis();
    }

    // ===========================
    // Click / Combat Methods
    // ===========================

    public void addClick() {
        long now = System.currentTimeMillis();
        clickTimestamps.addFirst(now);
        if (clickTimestamps.size() > MAX_CLICK_HISTORY) {
            clickTimestamps.removeLast();
        }
        lastAttackTime = now;
    }

    /**
     * Get clicks per second over the last N milliseconds.
     */
    public double getCPS(long windowMs) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;
        int count = 0;
        for (Long timestamp : clickTimestamps) {
            if (timestamp >= cutoff) {
                count++;
            } else {
                break; // Sorted descending, so we can stop
            }
        }
        return (count * 1000.0) / windowMs;
    }

    /**
     * Get click intervals for statistical analysis.
     */
    public double[] getClickIntervals(int count) {
        List<Double> intervals = new ArrayList<>();
        Iterator<Long> it = clickTimestamps.iterator();
        Long prev = null;
        int collected = 0;
        while (it.hasNext() && collected < count) {
            Long current = it.next();
            if (prev != null) {
                intervals.add((double) (prev - current));
                collected++;
            }
            prev = current;
        }
        return intervals.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // ===========================
    // Violation Methods
    // ===========================

    public int getViolationLevel(CheckType type) {
        return violations.getOrDefault(type, 0);
    }

    public int incrementViolation(CheckType type) {
        int newVL = violations.merge(type, 1, Integer::sum);
        return newVL;
    }

    public void decrementViolation(CheckType type, int amount) {
        violations.computeIfPresent(type, (k, v) -> Math.max(0, v - amount));
    }

    public void resetViolations(CheckType type) {
        violations.put(type, 0);
    }

    public void resetAllViolations() {
        for (CheckType type : CheckType.values()) {
            violations.put(type, 0);
        }
    }

    public Map<CheckType, Integer> getAllViolations() {
        return Collections.unmodifiableMap(violations);
    }

    // ===========================
    // Velocity Methods
    // ===========================

    public void setVelocity(double x, double y, double z) {
        this.lastVelocityX = x;
        this.lastVelocityY = y;
        this.lastVelocityZ = z;
        this.lastVelocityTime = System.currentTimeMillis();
        this.velocityPending = true;
        this.velocityTicks = 0;
    }

    public void tickVelocity() {
        if (velocityPending) {
            velocityTicks++;
            // Give the player 20 ticks to respond to velocity
            if (velocityTicks > 20) {
                velocityPending = false;
            }
        }
    }

    // ===========================
    // Teleport Handling
    // ===========================

    public void markTeleport() {
        this.teleporting = true;
        this.teleportTicks = 5; // Grace period of 5 ticks
    }

    // ===========================
    // Packet Tracking
    // ===========================

    public void incrementPacketCount() {
        totalPackets++;
        lastPacketTime = System.currentTimeMillis();
    }

    public int getTimerCancelTicks() {
        return timerCancelTicks;
    }

    public void setTimerCancelTicks(int timerCancelTicks) {
        this.timerCancelTicks = timerCancelTicks;
    }

    // ===========================
    // Getters & Setters
    // ===========================

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Location getLastLocation() { return lastLocation; }
    public Location getLastGroundLocation() { return lastGroundLocation; }
    public LinkedList<Location> getLocationHistory() { return locationHistory; }

    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaZ() { return deltaZ; }
    public double getLastDeltaX() { return lastDeltaX; }
    public double getLastDeltaY() { return lastDeltaY; }
    public double getLastDeltaZ() { return lastDeltaZ; }
    public float getDeltaYaw() { return deltaYaw; }
    public float getDeltaPitch() { return deltaPitch; }
    public float getLastDeltaYaw() { return lastDeltaYaw; }
    public float getLastDeltaPitch() { return lastDeltaPitch; }

    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public boolean isLastOnGround() { return lastOnGround; }
    public boolean isClientOnGround() { return clientOnGround; }
    public int getAirTicks() { return airTicks; }
    public int getGroundTicks() { return groundTicks; }
    public long getLastMovementTime() { return lastMovementTime; }

    public long getLastAttackTime() { return lastAttackTime; }
    public int getAttackTick() { return attackTick; }
    public void setAttackTick(int attackTick) { this.attackTick = attackTick; }
    public UUID getLastTarget() { return lastTarget; }
    public void setLastTarget(UUID lastTarget) { this.lastTarget = lastTarget; }

    public double getLastVelocityX() { return lastVelocityX; }
    public double getLastVelocityY() { return lastVelocityY; }
    public double getLastVelocityZ() { return lastVelocityZ; }
    public long getLastVelocityTime() { return lastVelocityTime; }
    public boolean isVelocityPending() { return velocityPending; }
    public void setVelocityPending(boolean pending) { this.velocityPending = pending; }
    public int getVelocityTicks() { return velocityTicks; }

    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean alertsEnabled) { this.alertsEnabled = alertsEnabled; }
    public boolean isVerboseEnabled() { return verboseEnabled; }
    public void setVerboseEnabled(boolean verboseEnabled) { this.verboseEnabled = verboseEnabled; }

    public long getJoinTime() { return joinTime; }
    public int getTotalPackets() { return totalPackets; }
    public long getLastPacketTime() { return lastPacketTime; }
    public boolean isTeleporting() { return teleporting; }

    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean inventoryOpen) { this.inventoryOpen = inventoryOpen; }
    public long getLastInventoryClose() { return lastInventoryClose; }
    public void setLastInventoryClose(long time) { this.lastInventoryClose = time; }

    public long getLastBlockPlace() { return lastBlockPlace; }
    public void setLastBlockPlace(long time) { this.lastBlockPlace = time; }
    public int getBlocksPlaced() { return blocksPlaced; }
    public void setBlocksPlaced(int count) { this.blocksPlaced = count; }
    public void incrementBlocksPlaced() { this.blocksPlaced++; }

    public int getScaffoldSignalCount() { return scaffoldSignalCount; }
    public void setScaffoldSignalCount(int count) { this.scaffoldSignalCount = count; }
    public void addScaffoldSignal() { this.scaffoldSignalCount++; }
    public boolean isPlacingBelow() { return placingBelow; }
    public void setPlacingBelow(boolean val) { this.placingBelow = val; }
    public float getLastPlacePitch() { return lastPlacePitch; }
    public void setLastPlacePitch(float pitch) { this.lastPlacePitch = pitch; }
    public List<Long> getPlacementIntervals() { return placementIntervals; }
    
    public long getLastScaffoldCancelTime() { return lastScaffoldCancelTime; }
    public void setLastScaffoldCancelTime(long time) { this.lastScaffoldCancelTime = time; }

    public boolean isDigging() { return digging; }
    public void setDigging(boolean digging) { this.digging = digging; }
    public long getLastDigStartTime() { return lastDigStartTime; }
    public void setLastDigStartTime(long time) { this.lastDigStartTime = time; }
    public long getDigStartTime() { return digStartTime; }
    public void setDigStartTime(long time) { this.digStartTime = time; }
    public int getDigBlockX() { return digBlockX; }
    public void setDigBlockX(int x) { this.digBlockX = x; }
    public int getDigBlockY() { return digBlockY; }
    public void setDigBlockY(int y) { this.digBlockY = y; }
    public int getDigBlockZ() { return digBlockZ; }
    public void setDigBlockZ(int z) { this.digBlockZ = z; }

    public long getTimerLastCheck() { return timerLastCheck; }
    public void setTimerLastCheck(long time) { this.timerLastCheck = time; }
    public int getTimerPacketCount() { return timerPacketCount; }
    public void setTimerPacketCount(int count) { this.timerPacketCount = count; }
}
