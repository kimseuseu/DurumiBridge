package com.durumi.bridge.api.handler;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatusHandler implements HttpHandler {

    private final DurumiBridge plugin;

    public StatusHandler(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }

        // Gather server info - some data must come from the main thread
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("online", true);
            status.put("server_name", "DurumiTown (두루미마을)");
            status.put("player_count", Bukkit.getOnlinePlayers().size());
            status.put("max_players", Bukkit.getMaxPlayers());

            // Calculate TPS - Paper provides this
            double[] tps = Bukkit.getTPS();
            status.put("tps_1m", Math.round(tps[0] * 100.0) / 100.0);
            status.put("tps_5m", Math.round(tps[1] * 100.0) / 100.0);
            status.put("tps_15m", Math.round(tps[2] * 100.0) / 100.0);

            status.put("version", Bukkit.getVersion());
            status.put("motd", Bukkit.getMotd());

            future.complete(status);
        });

        try {
            Map<String, Object> status = future.get();
            JsonUtil.sendJson(exchange, 200, status);
        } catch (Exception e) {
            JsonUtil.sendError(exchange, 500, "Failed to retrieve server status");
        }
    }
}
