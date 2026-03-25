package com.durumi.bridge.api.handler;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.util.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles announcement CRUD operations.
 * GET /api/announcements - List all (public)
 * POST /api/announcements - Create (requires API key)
 * DELETE /api/announcements/{id} - Delete (requires API key)
 */
public class AnnouncementHandler implements HttpHandler {

    private final DurumiBridge plugin;
    private final String apiKey;

    public AnnouncementHandler(DurumiBridge plugin, String apiKey) {
        this.plugin = plugin;
        this.apiKey = apiKey;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET" -> handleGet(exchange);
            case "POST" -> handlePost(exchange);
            case "DELETE" -> handleDelete(exchange, path);
            default -> JsonUtil.sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> announcements = plugin.getDatabaseManager().getAnnouncements();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", announcements.size());
        response.put("announcements", announcements);
        JsonUtil.sendJson(exchange, 200, response);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!checkApiKey(exchange)) {
            JsonUtil.sendError(exchange, 403, "Invalid or missing API key");
            return;
        }

        JsonObject body = JsonUtil.parseBody(exchange);
        if (body == null || !body.has("message")) {
            JsonUtil.sendError(exchange, 400, "Missing required field: message");
            return;
        }

        String message = body.get("message").getAsString();
        String author = body.has("author") ? body.get("author").getAsString() : "System";

        if (message.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Message cannot be empty");
            return;
        }

        int id = plugin.getDatabaseManager().createAnnouncement(message, author);
        if (id > 0) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("id", id);
            response.put("message", message);
            response.put("author", author);
            JsonUtil.sendJson(exchange, 201, response);
        } else {
            JsonUtil.sendError(exchange, 500, "Failed to create announcement");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (!checkApiKey(exchange)) {
            JsonUtil.sendError(exchange, 403, "Invalid or missing API key");
            return;
        }

        // Extract ID from path: /api/announcements/{id}
        String prefix = "/api/announcements/";
        if (!path.startsWith(prefix) || path.length() <= prefix.length()) {
            JsonUtil.sendError(exchange, 400, "Missing announcement ID");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            JsonUtil.sendError(exchange, 400, "Invalid announcement ID");
            return;
        }

        boolean deleted = plugin.getDatabaseManager().deleteAnnouncement(id);
        if (deleted) {
            JsonUtil.sendSuccess(exchange, "Announcement " + id + " deleted");
        } else {
            JsonUtil.sendError(exchange, 404, "Announcement not found");
        }
    }

    private boolean checkApiKey(HttpExchange exchange) {
        String key = exchange.getRequestHeaders().getFirst("X-API-Key");
        return apiKey != null && apiKey.equals(key);
    }
}
