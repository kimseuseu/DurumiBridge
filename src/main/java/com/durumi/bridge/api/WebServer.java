package com.durumi.bridge.api;

import com.durumi.bridge.DurumiBridge;
import com.durumi.bridge.api.handler.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class WebServer {

    private final DurumiBridge plugin;
    private final int port;
    private final String apiKey;
    private final List<String> allowedOrigins;
    private HttpServer server;

    public WebServer(DurumiBridge plugin, int port, String apiKey, List<String> allowedOrigins) {
        this.plugin = plugin;
        this.port = port;
        this.apiKey = apiKey;
        this.allowedOrigins = allowedOrigins;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        // Register handlers wrapped with CORS
        server.createContext("/api/status", cors(new StatusHandler(plugin)));
        server.createContext("/api/players", cors(new PlayerHandler(plugin)));
        server.createContext("/api/map/tiles", cors(new MapHandler(plugin)));
        server.createContext("/api/announcements", cors(new AnnouncementHandler(plugin, apiKey)));
        server.createContext("/api/board", cors(new BoardHandler(plugin)));
        server.createContext("/api/verify", cors(new VerifyHandler(plugin)));

        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Wraps a handler with CORS headers and OPTIONS preflight handling.
     */
    private HttpHandler cors(HttpHandler handler) {
        return exchange -> {
            try {
                setCorsHeaders(exchange);

                // Handle preflight
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                handler.handle(exchange);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error handling request: " + exchange.getRequestURI(), e);
                try {
                    String error = "{\"error\":true,\"message\":\"Internal server error\"}";
                    byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(500, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (IOException ignored) {
                    // Response already started or connection closed
                }
            } finally {
                exchange.close();
            }
        };
    }

    private void setCorsHeaders(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin != null && (allowedOrigins.contains("*") || allowedOrigins.contains(origin))) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        } else if (!allowedOrigins.isEmpty()) {
            // Default to first allowed origin if no match
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowedOrigins.get(0));
        }

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-API-Key, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    public String getApiKey() {
        return apiKey;
    }
}
