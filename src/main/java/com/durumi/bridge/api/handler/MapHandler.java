package com.durumi.bridge.api.handler;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Serves pre-rendered map tile PNG images.
 * URL format: /api/map/tiles/{world}/{z}/{x}/{y}.png
 */
public class MapHandler implements HttpHandler {

    private final DurumiBridge plugin;

    public MapHandler(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Expected: /api/map/tiles/{world}/{z}/{x}/{y}.png
        String prefix = "/api/map/tiles/";
        if (!path.startsWith(prefix)) {
            JsonUtil.sendError(exchange, 400, "Invalid tile path");
            return;
        }

        String tilePath = path.substring(prefix.length());
        // Remove .png extension if present
        if (tilePath.endsWith(".png")) {
            tilePath = tilePath.substring(0, tilePath.length() - 4);
        }

        String[] parts = tilePath.split("/");
        if (parts.length != 4) {
            JsonUtil.sendError(exchange, 400, "Invalid tile path format. Expected: {world}/{z}/{x}/{y}.png");
            return;
        }

        String worldName = parts[0];
        int zoom, x, y;
        try {
            zoom = Integer.parseInt(parts[1]);
            x = Integer.parseInt(parts[2]);
            y = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(exchange, 400, "Invalid tile coordinates");
            return;
        }

        File tileFile = plugin.getMapRenderer().getTileFile(worldName, zoom, x, y);
        if (tileFile == null) {
            // Return a transparent 1x1 PNG for missing tiles
            JsonUtil.sendError(exchange, 404, "Tile not found");
            return;
        }

        byte[] data = Files.readAllBytes(tileFile.toPath());
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
