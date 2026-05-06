package com.modmc.anticheat.webhook;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.PlayerUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Discord webhook integration for sending violation alerts.
 */
public class DiscordWebhook {

    private final ModMCAntiCheat plugin;
    private boolean enabled;
    private String webhookUrl;
    private int color;
    private int minVl;
    private int rateLimit;
    private final Map<UUID, Long> lastMessageTimes = new ConcurrentHashMap<>();

    public DiscordWebhook(ModMCAntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("discord.enabled", false);
        this.webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        if (this.webhookUrl != null) {
            this.webhookUrl = this.webhookUrl.trim();
        }
        this.color = plugin.getConfig().getInt("discord.color", 16711680);
        this.minVl = plugin.getConfig().getInt("discord.min-vl", 10);
        this.rateLimit = plugin.getConfig().getInt("discord.rate-limit", 30);

        if (enabled && webhookUrl != null && !webhookUrl.isEmpty()
                && !webhookUrl.contains("YOUR_WEBHOOK_URL")) {
            plugin.getLogger().info("[Discord] Webhook enabled.");
        }
    }

    /**
     * Send a violation alert to Discord.
     */
    public void sendViolation(PlayerData data, CheckType checkType, int vl, String details) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()
                || webhookUrl.contains("YOUR_WEBHOOK_URL")) return;

        if (vl < minVl) return;

        UUID uuid = data.getUuid();
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTimes.get(uuid);
        if (lastTime != null && (now - lastTime) < (rateLimit * 1000L)) {
            return;
        }
        lastMessageTimes.put(uuid, now);

        String playerName = data.getName();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int ping = 0;
                try {
                    if (data.getPlayer() != null) {
                        ping = PlayerUtil.getPing(data.getPlayer());
                    }
                } catch (Exception ignored) {}

                String json = buildEmbed(playerName, checkType.getDisplayName(),
                        checkType.getCategory(), vl, ping, details);
                sendJson(json);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[Discord] Failed to send: " + e.getMessage());
            }
        });
    }

    /**
     * Send a punishment notification to Discord.
     */
    public void sendPunishment(String playerName, String checkName, String action) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()
                || webhookUrl.contains("YOUR_WEBHOOK_URL")) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String json = "{\"embeds\": [{\"title\": \"Player Punished\","
                        + "\"color\": 15158332,"
                        + "\"fields\": ["
                        + "{\"name\": \"Player\", \"value\": \"" + escapeJson(playerName) + "\", \"inline\": true},"
                        + "{\"name\": \"Check\", \"value\": \"" + escapeJson(checkName) + "\", \"inline\": true},"
                        + "{\"name\": \"Action\", \"value\": \"" + escapeJson(action) + "\", \"inline\": true}"
                        + "],"
                        + "\"thumbnail\": {\"url\": \"https://minotar.net/avatar/" + escapeJson(playerName) + "/128\"},"
                        + "\"footer\": {\"text\": \"ModMC AntiCheat\"}"
                        + "}]}";
                sendJson(json);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[Discord] Failed to send punishment: " + e.getMessage());
            }
        });
    }

    private String buildEmbed(String playerName, String checkName, String category,
                              int vl, int ping, String details) {
        String safeDetails = details != null ? escapeJson(details) : "N/A";
        String safePlayer = escapeJson(playerName);

        return "{\"embeds\": [{\"title\": \"AntiCheat Alert\","
                + "\"color\": " + color + ","
                + "\"fields\": ["
                + "{\"name\": \"Player\", \"value\": \"" + safePlayer + "\", \"inline\": true},"
                + "{\"name\": \"Check\", \"value\": \"" + checkName + "\", \"inline\": true},"
                + "{\"name\": \"Category\", \"value\": \"" + category + "\", \"inline\": true},"
                + "{\"name\": \"VL\", \"value\": \"" + vl + "\", \"inline\": true},"
                + "{\"name\": \"Ping\", \"value\": \"" + ping + "ms\", \"inline\": true},"
                + "{\"name\": \"Details\", \"value\": \"" + safeDetails + "\", \"inline\": false}"
                + "],"
                + "\"thumbnail\": {\"url\": \"https://minotar.net/avatar/" + safePlayer + "/128\"},"
                + "\"footer\": {\"text\": \"ModMC AntiCheat\"}"
                + "}]}";
    }

    /**
     * Send a JSON payload to the webhook URL.
     * Handles redirects manually to preserve POST method.
     */
    private void sendJson(String json) throws Exception {
        String targetUrl = webhookUrl;
        int maxRedirects = 5;

        for (int i = 0; i < maxRedirects; i++) {
            URL url = URI.create(targetUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false); // Handle redirects manually
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("User-Agent", "ModMC-AntiCheat/1.0 (compatible; Java)");
            connection.setRequestProperty("Accept", "*/*");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(input.length);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(input);
                os.flush();
            }

            int responseCode = connection.getResponseCode();

            // Handle redirects — re-POST to the new location
            if (responseCode == 301 || responseCode == 302 || responseCode == 307 || responseCode == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location != null) {
                    targetUrl = location;
                    plugin.getLogger().info("[Discord] Following redirect to: " + location);
                    continue;
                }
            }

            if (responseCode == 200 || responseCode == 204) {
                connection.disconnect();
                return; // Success
            }

            // Read error body
            String errorBody = "";
            try {
                if (connection.getErrorStream() != null) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        errorBody = sb.toString();
                    }
                }
            } catch (Exception ignored) {}

            plugin.getLogger().warning("[Discord] HTTP " + responseCode + " | URL: " + targetUrl + " | Body: " + errorBody);
            connection.disconnect();
            return;
        }
    }

    /**
     * Send a test message to verify webhook is working.
     */
    public void sendTest() {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()
                || webhookUrl.contains("YOUR_WEBHOOK_URL")) {
            plugin.getLogger().warning("[Discord] Webhook is not configured. Check config.yml");
            return;
        }

        plugin.getLogger().info("[Discord] Testing webhook URL: " + webhookUrl);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String json = "{\"embeds\": [{\"title\": \"ModMC AntiCheat Connected!\","
                        + "\"description\": \"Discord webhook is working correctly.\","
                        + "\"color\": 5763719,"
                        + "\"footer\": {\"text\": \"ModMC AntiCheat\"}"
                        + "}]}";
                sendJson(json);
                plugin.getLogger().info("[Discord] Test message sent successfully!");
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Test failed: " + e.getMessage());
            }
        });
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
