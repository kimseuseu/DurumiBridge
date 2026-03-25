package com.durumi.bridge.api.handler;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.util.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles MC account verification.
 * POST /api/verify - Verify a Minecraft account using username + code
 */
public class VerifyHandler implements HttpHandler {

    private final DurumiBridge plugin;

    public VerifyHandler(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonUtil.sendError(exchange, 405, "Method not allowed. Use POST.");
            return;
        }

        JsonObject body = JsonUtil.parseBody(exchange);
        if (body == null) {
            JsonUtil.sendError(exchange, 400, "Invalid JSON body");
            return;
        }

        if (!body.has("username") || !body.has("code")) {
            JsonUtil.sendError(exchange, 400, "Missing required fields: username, code");
            return;
        }

        String username = body.get("username").getAsString().trim();
        String code = body.get("code").getAsString().trim();

        if (username.isBlank() || code.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Username and code cannot be empty");
            return;
        }

        Map<String, Object> result = plugin.getDatabaseManager().verifyCode(username, code);
        if (result != null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Account verified successfully!");
            response.put("username", result.get("username"));
            response.put("uuid", result.get("uuid"));
            response.put("token", result.get("token"));
            JsonUtil.sendJson(exchange, 200, response);
        } else {
            JsonUtil.sendError(exchange, 401, "Invalid username or verification code. Generate a new code with /durumi verify in-game.");
        }
    }
}
