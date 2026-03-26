package com.durumi.bridge;

import com.durumi.bridge.api.WebServer;
import com.durumi.bridge.command.DurumiCommand;
import com.durumi.bridge.data.DatabaseManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

public class DurumiBridge extends JavaPlugin {

    private static DurumiBridge instance;
    private WebServer webServer;
    private DatabaseManager databaseManager;
    private int syncTaskId = -1;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Initialize database
        String dbPath = getConfig().getString("database.path", "durumi.db");
        databaseManager = new DatabaseManager(getDataFolder(), dbPath);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Database initialized successfully.");

        // Start web server
        int port = getConfig().getInt("api.port", 8080);
        String apiKey = getConfig().getString("api.api-key", "CHANGE-ME-TO-A-SECURE-KEY");
        List<String> allowedOrigins = getConfig().getStringList("cors.allowed-origins");

        webServer = new WebServer(this, port, apiKey, allowedOrigins);
        try {
            webServer.start();
            getLogger().info("Web API server started on port " + port);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start web server!", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        DurumiCommand command = new DurumiCommand(this);
        getCommand("durumi").setExecutor(command);
        getCommand("durumi").setTabCompleter(command);

        // Start website sync
        startSyncTask();

        getLogger().info("DurumiBridge enabled! 두루미마을 브릿지가 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
            getLogger().info("Web API server stopped.");
        }

        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Database connection closed.");
        }

        getLogger().info("DurumiBridge disabled.");
        instance = null;
    }

    public void reloadPlugin() {
        // Stop existing services
        if (webServer != null) {
            webServer.stop();
        }

        // Reload configuration
        reloadConfig();

        // Restart web server
        int port = getConfig().getInt("api.port", 8080);
        String apiKey = getConfig().getString("api.api-key", "CHANGE-ME-TO-A-SECURE-KEY");
        List<String> allowedOrigins = getConfig().getStringList("cors.allowed-origins");

        webServer = new WebServer(this, port, apiKey, allowedOrigins);
        try {
            webServer.start();
            getLogger().info("Web API server restarted on port " + port);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to restart web server!", e);
        }

        // Restart sync
        startSyncTask();

        getLogger().info("DurumiBridge reloaded!");
    }

    private void startSyncTask() {
        // Cancel existing sync task if running
        if (syncTaskId != -1) {
            Bukkit.getScheduler().cancelTask(syncTaskId);
            syncTaskId = -1;
        }

        if (!getConfig().getBoolean("sync.enabled", false)) return;

        int interval = getConfig().getInt("sync.interval", 30) * 20; // ticks
        String syncUrl = getConfig().getString("sync.url", "");
        String syncSecret = getConfig().getString("sync.secret", "");

        if (syncUrl.isEmpty()) return;

        syncTaskId = getServer().getScheduler().runTaskTimer(this, () -> {
            // Collect data on main thread
            JsonObject json = new JsonObject();
            json.addProperty("online", true);

            JsonObject players = new JsonObject();
            players.addProperty("online", Bukkit.getOnlinePlayers().size());
            players.addProperty("max", Bukkit.getMaxPlayers());

            JsonArray playerList = new JsonArray();
            for (Player p : Bukkit.getOnlinePlayers()) {
                JsonObject pj = new JsonObject();
                pj.addProperty("name", p.getName());
                pj.addProperty("uuid", p.getUniqueId().toString());
                pj.addProperty("world", p.getWorld().getName());
                pj.addProperty("x", Math.round(p.getLocation().getX()));
                pj.addProperty("y", Math.round(p.getLocation().getY()));
                pj.addProperty("z", Math.round(p.getLocation().getZ()));
                pj.addProperty("health", Math.round(p.getHealth()));
                playerList.add(pj);
            }
            players.add("list", playerList);
            json.add("players", players);

            json.addProperty("version", Bukkit.getMinecraftVersion());
            json.addProperty("motd", Bukkit.getMotd());
            json.addProperty("tps", Math.round(Bukkit.getTPS()[0] * 100.0) / 100.0);

            String jsonStr = json.toString();

            // Send async
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    URL url = new URL(syncUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + syncSecret);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
                    }
                    int code = conn.getResponseCode();
                    if (code != 200) {
                        getLogger().warning("[Sync] HTTP " + code);
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    getLogger().warning("[Sync] Failed: " + e.getMessage());
                }
            });
        }, 100L, (long) interval).getTaskId();

        getLogger().info("[Sync] Website sync enabled: " + syncUrl + " (every " + getConfig().getInt("sync.interval", 30) + "s)");
    }

    public static DurumiBridge getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
