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
        String query = exchange.getRequestURI().getQuery();
        int page = 1;
        int limit = 10;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    try {
                        if ("page".equals(kv[0])) page = Math.max(1, Integer.parseInt(kv[1]));
                        else if ("limit".equals(kv[0])) limit = Math.max(1, Math.min(100, Integer.parseInt(kv[1])));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        int total = plugin.getDatabaseManager().getAnnouncementCount();
        int totalPages = (int) Math.ceil((double) total / limit);
        List<Map<String, Object>> announcements = plugin.getDatabaseManager().getPaginatedAnnouncements(page, limit);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", announcements);
        response.put("page", page);
        response.put("totalPages", totalPages);
        response.put("total", total);
        JsonUtil.sendJson(exchange, 200, response);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!checkApiKey(exchange)) {
            JsonUtil.sendError(exchange, 403, "Invalid or missing API key");
            return;
        }

        JsonObject body = JsonUtil.parseBody(exchange);
        if (body == null || !body.has("title") || !body.has("content")) {
            JsonUtil.sendError(exchange, 400, "Missing required fields: title, content");
            return;
        }

        String title = body.get("title").getAsString();
        String content = body.get("content").getAsString();
        String author = body.has("author") ? body.get("author").getAsString() : "System";
        String category = body.has("category") ? body.get("category").getAsString() : "일반";
        boolean pinned = body.has("pinned") && body.get("pinned").getAsBoolean();

        if (title.isBlank() || content.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Title and content cannot be empty");
            return;
        }

        int id = plugin.getDatabaseManager().createAnnouncement(title, content, author, category, pinned);
        if (id > 0) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("id", id);
            response.put("title", title);
            response.put("content", content);
            response.put("author", author);
            response.put("category", category);
            response.put("pinned", pinned);
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
