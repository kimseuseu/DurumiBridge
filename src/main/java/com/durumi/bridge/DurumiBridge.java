package com.durumi.bridge;

import com.durumi.bridge.api.WebServer;
import com.durumi.bridge.command.DurumiCommand;
import com.durumi.bridge.data.DatabaseManager;
import com.durumi.bridge.map.MapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class DurumiBridge extends JavaPlugin {

    private static DurumiBridge instance;
    private WebServer webServer;
    private DatabaseManager databaseManager;
    private MapRenderer mapRenderer;

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

        // Initialize map renderer
        mapRenderer = new MapRenderer(this);
        scheduleMapRendering();

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

        // Reschedule map rendering
        scheduleMapRendering();

        getLogger().info("DurumiBridge reloaded!");
    }

    private void scheduleMapRendering() {
        int intervalMinutes = getConfig().getInt("map.render-interval", 60);
        long intervalTicks = intervalMinutes * 60L * 20L;

        // Cancel existing tasks
        Bukkit.getScheduler().cancelTasks(this);

        // Schedule periodic map rendering (async for the file I/O, but chunk access on main thread)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            List<String> worlds = getConfig().getStringList("map.worlds");
            int radius = getConfig().getInt("map.render-radius", 50);
            for (String worldName : worlds) {
                mapRenderer.renderWorld(worldName, radius);
            }
        }, 100L, intervalTicks); // Start 5 seconds after enable, then repeat
    }

    public static DurumiBridge getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
