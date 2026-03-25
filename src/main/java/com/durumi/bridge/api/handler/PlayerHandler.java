package com.durumi.bridge.api.handler;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerHandler implements HttpHandler {

    private final DurumiBridge plugin;

    public PlayerHandler(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }

        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            List<Map<String, Object>> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("name", player.getName());
                info.put("uuid", player.getUniqueId().toString());
                info.put("display_name", player.getDisplayName());

                Location loc = player.getLocation();
                info.put("world", loc.getWorld().getName());
                info.put("x", Math.round(loc.getX() * 100.0) / 100.0);
                info.put("y", Math.round(loc.getY() * 100.0) / 100.0);
                info.put("z", Math.round(loc.getZ() * 100.0) / 100.0);

                info.put("health", Math.round(player.getHealth() * 10.0) / 10.0);
                info.put("max_health", Math.round(player.getMaxHealth() * 10.0) / 10.0);
                info.put("food_level", player.getFoodLevel());
                info.put("game_mode", player.getGameMode().name());

                // Skin URL from Crafatar (a popular Minecraft avatar API)
                String uuid = player.getUniqueId().toString().replace("-", "");
                info.put("skin_url", "https://crafatar.com/avatars/" + uuid + "?overlay=true");
                info.put("skin_body_url", "https://crafatar.com/renders/body/" + uuid + "?overlay=true");

                players.add(info);
            }
            future.complete(players);
        });

        try {
            List<Map<String, Object>> players = future.get();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("count", players.size());
            response.put("players", players);
            JsonUtil.sendJson(exchange, 200, response);
        } catch (Exception e) {
            JsonUtil.sendError(exchange, 500, "Failed to retrieve player list");
        }
    }
}
