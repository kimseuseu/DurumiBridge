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
 * Handles forum board operations.
 * GET /api/board - List posts
 * GET /api/board/{id} - Single post with comments
 * POST /api/board - Create post (requires verification token)
 * POST /api/board/{id}/comments - Add comment (requires verification token)
 */
public class BoardHandler implements HttpHandler {

    private final DurumiBridge plugin;

    public BoardHandler(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method)) {
            handleGet(exchange, path);
        } else if ("POST".equals(method)) {
            handlePost(exchange, path);
        } else {
            JsonUtil.sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String prefix = "/api/board";

        // GET /api/board - list all posts
        if (path.equals(prefix) || path.equals(prefix + "/")) {
            List<Map<String, Object>> posts = plugin.getDatabaseManager().getBoardPosts();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("count", posts.size());
            response.put("posts", posts);
            JsonUtil.sendJson(exchange, 200, response);
            return;
        }

        // GET /api/board/{id} - single post with comments
        String idStr = path.substring(prefix.length() + 1);
        // Remove trailing slash if present
        if (idStr.endsWith("/")) {
            idStr = idStr.substring(0, idStr.length() - 1);
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(exchange, 400, "Invalid post ID");
            return;
        }

        Map<String, Object> post = plugin.getDatabaseManager().getBoardPost(id);
        if (post == null) {
            JsonUtil.sendError(exchange, 404, "Post not found");
            return;
        }

        List<Map<String, Object>> comments = plugin.getDatabaseManager().getComments(id);
        post.put("comments", comments);
        post.put("comment_count", comments.size());
        JsonUtil.sendJson(exchange, 200, post);
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        String prefix = "/api/board";

        // Check for comment creation: POST /api/board/{id}/comments
        if (path.matches("/api/board/\\d+/comments/?")) {
            handleAddComment(exchange, path);
            return;
        }

        // POST /api/board - create new post
        if (path.equals(prefix) || path.equals(prefix + "/")) {
            handleCreatePost(exchange);
            return;
        }

        JsonUtil.sendError(exchange, 400, "Invalid board endpoint");
    }

    private void handleCreatePost(HttpExchange exchange) throws IOException {
        JsonObject body = JsonUtil.parseBody(exchange);
        if (body == null) {
            JsonUtil.sendError(exchange, 400, "Invalid JSON body");
            return;
        }

        // Verify the user
        String token = null;
        if (body.has("token")) {
            token = body.get("token").getAsString();
        }

        if (token == null || token.isBlank()) {
            JsonUtil.sendError(exchange, 401, "Verification token required. Link your MC account first via /durumi verify");
            return;
        }

        Map<String, Object> verifiedUser = plugin.getDatabaseManager().getVerifiedUser(token);
        if (verifiedUser == null) {
            JsonUtil.sendError(exchange, 401, "Invalid verification token");
            return;
        }

        if (!body.has("title") || !body.has("content")) {
            JsonUtil.sendError(exchange, 400, "Missing required fields: title, content");
            return;
        }

        String title = body.get("title").getAsString();
        String content = body.get("content").getAsString();

        if (title.isBlank() || content.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Title and content cannot be empty");
            return;
        }

        String author = (String) verifiedUser.get("username");
        String authorUuid = (String) verifiedUser.get("uuid");

        int id = plugin.getDatabaseManager().createBoardPost(title, content, author, authorUuid);
        if (id > 0) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("id", id);
            response.put("title", title);
            response.put("author", author);
            JsonUtil.sendJson(exchange, 201, response);
        } else {
            JsonUtil.sendError(exchange, 500, "Failed to create post");
        }
    }

    private void handleAddComment(HttpExchange exchange, String path) throws IOException {
        // Extract post ID from /api/board/{id}/comments
        String[] parts = path.split("/");
        int postId;
        try {
            postId = Integer.parseInt(parts[3]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            JsonUtil.sendError(exchange, 400, "Invalid post ID");
            return;
        }

        // Check the post exists
        Map<String, Object> post = plugin.getDatabaseManager().getBoardPost(postId);
        if (post == null) {
            JsonUtil.sendError(exchange, 404, "Post not found");
            return;
        }

        JsonObject body = JsonUtil.parseBody(exchange);
        if (body == null) {
            JsonUtil.sendError(exchange, 400, "Invalid JSON body");
            return;
        }

        // Verify the user
        String token = null;
        if (body.has("token")) {
            token = body.get("token").getAsString();
        }

        if (token == null || token.isBlank()) {
            JsonUtil.sendError(exchange, 401, "Verification token required");
            return;
        }

        Map<String, Object> verifiedUser = plugin.getDatabaseManager().getVerifiedUser(token);
        if (verifiedUser == null) {
            JsonUtil.sendError(exchange, 401, "Invalid verification token");
            return;
        }

        if (!body.has("content")) {
            JsonUtil.sendError(exchange, 400, "Missing required field: content");
            return;
        }

        String content = body.get("content").getAsString();
        if (content.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Content cannot be empty");
            return;
        }

        String author = (String) verifiedUser.get("username");
        String authorUuid = (String) verifiedUser.get("uuid");

        int commentId = plugin.getDatabaseManager().createComment(postId, content, author, authorUuid);
        if (commentId > 0) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("id", commentId);
            response.put("post_id", postId);
            response.put("author", author);
            JsonUtil.sendJson(exchange, 201, response);
        } else {
            JsonUtil.sendError(exchange, 500, "Failed to create comment");
        }
    }
}
