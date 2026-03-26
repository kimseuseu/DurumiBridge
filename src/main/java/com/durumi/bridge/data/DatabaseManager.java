package com.durumi.bridge.data;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private final File dataFolder;
    private final String dbFileName;
    private Connection connection;
    private final Logger logger = Logger.getLogger("DurumiBridge");

    public DatabaseManager(File dataFolder, String dbFileName) {
        this.dataFolder = dataFolder;
        this.dbFileName = dbFileName;
    }

    public boolean initialize() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, dbFileName);
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(true);

            // Enable WAL mode for better concurrent reads
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
            }

            createTables();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SQLite database", e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS announcements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    author TEXT NOT NULL,
                    category TEXT DEFAULT '일반',
                    pinned INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS board_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    author TEXT NOT NULL,
                    author_uuid TEXT,
                    category TEXT DEFAULT '자유',
                    views INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS board_comments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    post_id INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    author TEXT NOT NULL,
                    author_uuid TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (post_id) REFERENCES board_posts(id) ON DELETE CASCADE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS verification_codes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    uuid TEXT NOT NULL,
                    code TEXT NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    verified INTEGER DEFAULT 0,
                    token TEXT
                )
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_comments_post_id ON board_comments(post_id)
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_verification_code ON verification_codes(code)
            """);
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing database", e);
        }
    }

    // ---- Announcements ----

    public synchronized List<Map<String, Object>> getAnnouncements() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, title, content, author, category, pinned, created_at FROM announcements ORDER BY pinned DESC, created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("author", rs.getString("author"));
                row.put("category", rs.getString("category"));
                row.put("pinned", rs.getInt("pinned") == 1);
                row.put("createdAt", rs.getString("created_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching announcements", e);
        }
        return list;
    }

    public synchronized List<Map<String, Object>> getPaginatedAnnouncements(int page, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        int offset = (page - 1) * limit;
        String sql = "SELECT id, title, content, author, category, pinned, created_at FROM announcements ORDER BY pinned DESC, created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("title", rs.getString("title"));
                    row.put("content", rs.getString("content"));
                    row.put("author", rs.getString("author"));
                    row.put("category", rs.getString("category"));
                    row.put("pinned", rs.getInt("pinned") == 1);
                    row.put("createdAt", rs.getString("created_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching paginated announcements", e);
        }
        return list;
    }

    public synchronized int getAnnouncementCount() {
        String sql = "SELECT COUNT(*) FROM announcements";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error counting announcements", e);
        }
        return 0;
    }

    public synchronized int createAnnouncement(String title, String content, String author, String category, boolean pinned) {
        String sql = "INSERT INTO announcements (title, content, author, category, pinned) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, author);
            ps.setString(4, category);
            ps.setInt(5, pinned ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error creating announcement", e);
        }
        return -1;
    }

    public synchronized boolean deleteAnnouncement(int id) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error deleting announcement", e);
            return false;
        }
    }

    // ---- Board Posts ----

    public synchronized List<Map<String, Object>> getBoardPosts() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.id, p.title, p.content, p.author, p.author_uuid, p.category, p.views, p.created_at, p.updated_at, " +
                "(SELECT COUNT(*) FROM board_comments c WHERE c.post_id = p.id) AS comment_count " +
                "FROM board_posts p ORDER BY p.created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("author", rs.getString("author"));
                row.put("author_uuid", rs.getString("author_uuid"));
                row.put("category", rs.getString("category"));
                row.put("views", rs.getInt("views"));
                row.put("comments", rs.getInt("comment_count"));
                row.put("createdAt", rs.getString("created_at"));
                row.put("updatedAt", rs.getString("updated_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching board posts", e);
        }
        return list;
    }

    public synchronized List<Map<String, Object>> getPaginatedBoardPosts(int page, int limit, String category) {
        List<Map<String, Object>> list = new ArrayList<>();
        int offset = (page - 1) * limit;
        String sql;
        if (category != null && !category.isBlank()) {
            sql = "SELECT p.id, p.title, p.content, p.author, p.author_uuid, p.category, p.views, p.created_at, p.updated_at, " +
                    "(SELECT COUNT(*) FROM board_comments c WHERE c.post_id = p.id) AS comment_count " +
                    "FROM board_posts p WHERE p.category = ? ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        } else {
            sql = "SELECT p.id, p.title, p.content, p.author, p.author_uuid, p.category, p.views, p.created_at, p.updated_at, " +
                    "(SELECT COUNT(*) FROM board_comments c WHERE c.post_id = p.id) AS comment_count " +
                    "FROM board_posts p ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            if (category != null && !category.isBlank()) {
                ps.setString(paramIndex++, category);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("title", rs.getString("title"));
                    row.put("content", rs.getString("content"));
                    row.put("author", rs.getString("author"));
                    row.put("author_uuid", rs.getString("author_uuid"));
                    row.put("category", rs.getString("category"));
                    row.put("views", rs.getInt("views"));
                    row.put("comments", rs.getInt("comment_count"));
                    row.put("createdAt", rs.getString("created_at"));
                    row.put("updatedAt", rs.getString("updated_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching paginated board posts", e);
        }
        return list;
    }

    public synchronized int getBoardPostCount(String category) {
        String sql;
        if (category != null && !category.isBlank()) {
            sql = "SELECT COUNT(*) FROM board_posts WHERE category = ?";
        } else {
            sql = "SELECT COUNT(*) FROM board_posts";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (category != null && !category.isBlank()) {
                ps.setString(1, category);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error counting board posts", e);
        }
        return 0;
    }

    public synchronized void incrementPostViews(int id) {
        String sql = "UPDATE board_posts SET views = views + 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error incrementing views for post id=" + id, e);
        }
    }

    public synchronized Map<String, Object> getBoardPost(int id) {
        String sql = "SELECT id, title, content, author, author_uuid, category, views, created_at, updated_at FROM board_posts WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("title", rs.getString("title"));
                    row.put("content", rs.getString("content"));
                    row.put("author", rs.getString("author"));
                    row.put("author_uuid", rs.getString("author_uuid"));
                    row.put("category", rs.getString("category"));
                    row.put("views", rs.getInt("views"));
                    row.put("createdAt", rs.getString("created_at"));
                    row.put("updatedAt", rs.getString("updated_at"));
                    return row;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching board post id=" + id, e);
        }
        return null;
    }

    public synchronized int createBoardPost(String title, String content, String author, String authorUuid, String category) {
        String sql = "INSERT INTO board_posts (title, content, author, author_uuid, category) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, author);
            ps.setString(4, authorUuid);
            ps.setString(5, category);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error creating board post", e);
        }
        return -1;
    }

    // ---- Comments ----

    public synchronized List<Map<String, Object>> getComments(int postId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, post_id, content, author, author_uuid, created_at FROM board_comments WHERE post_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("post_id", rs.getInt("post_id"));
                    row.put("content", rs.getString("content"));
                    row.put("author", rs.getString("author"));
                    row.put("author_uuid", rs.getString("author_uuid"));
                    row.put("created_at", rs.getString("created_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching comments for post " + postId, e);
        }
        return list;
    }

    public synchronized int createComment(int postId, String content, String author, String authorUuid) {
        String sql = "INSERT INTO board_comments (post_id, content, author, author_uuid) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, postId);
            ps.setString(2, content);
            ps.setString(3, author);
            ps.setString(4, authorUuid);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error creating comment", e);
        }
        return -1;
    }

    // ---- Verification ----

    public synchronized String createVerificationCode(String username, String uuid, String code) {
        // Remove any existing unverified codes for this player
        String deleteSql = "DELETE FROM verification_codes WHERE uuid = ? AND verified = 0";
        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error cleaning old verification codes", e);
        }

        String sql = "INSERT INTO verification_codes (username, uuid, code) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, uuid);
            ps.setString(3, code);
            ps.executeUpdate();
            return code;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error creating verification code", e);
            return null;
        }
    }

    public synchronized Map<String, Object> verifyCode(String username, String code) {
        String sql = "SELECT id, username, uuid FROM verification_codes WHERE username = ? AND code = ? AND verified = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String uuid = rs.getString("uuid");
                    String name = rs.getString("username");

                    // Generate a token
                    String token = java.util.UUID.randomUUID().toString();

                    // Mark as verified
                    String updateSql = "UPDATE verification_codes SET verified = 1, token = ? WHERE id = ?";
                    try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                        updatePs.setString(1, token);
                        updatePs.setInt(2, id);
                        updatePs.executeUpdate();
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("username", name);
                    result.put("uuid", uuid);
                    result.put("token", token);
                    return result;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error verifying code", e);
        }
        return null;
    }

    public synchronized Map<String, Object> getVerifiedUser(String token) {
        String sql = "SELECT username, uuid FROM verification_codes WHERE token = ? AND verified = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("username", rs.getString("username"));
                    result.put("uuid", rs.getString("uuid"));
                    return result;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching verified user", e);
        }
        return null;
    }
}
