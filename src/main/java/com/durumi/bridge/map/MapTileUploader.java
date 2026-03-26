package com.durumi.bridge.map;

import com.durumi.bridge.DurumiBridge;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

/**
 * Uploads rendered map tiles to the DurumiTown website's Vercel Blob Storage.
 * Tiles are sent as base64-encoded PNGs in batches to /api/map/upload.
 */
public class MapTileUploader {

    private static final int BATCH_SIZE = 20;

    private final DurumiBridge plugin;

    public MapTileUploader(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    /**
     * Upload all tiles from the tiles directory asynchronously.
     * Call this after map rendering is complete.
     */
    public void uploadTilesAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::uploadTiles);
    }

    private void uploadTiles() {
        String syncUrl = plugin.getConfig().getString("sync.url", "");
        String syncSecret = plugin.getConfig().getString("sync.secret", "");

        if (syncUrl.isEmpty() || syncSecret.isEmpty()) {
            plugin.getLogger().warning("[MapUpload] sync.url or sync.secret not configured, skipping tile upload.");
            return;
        }

        // Derive upload URL from sync URL: replace /api/sync with /api/map/upload
        String uploadUrl = syncUrl.replace("/api/sync", "/api/map/upload");

        File tilesRoot = plugin.getMapRenderer().getTilesRoot();
        if (!tilesRoot.exists()) {
            plugin.getLogger().warning("[MapUpload] Tiles directory does not exist: " + tilesRoot.getPath());
            return;
        }

        // Collect all tile files: tiles/{world}/{zoom}/{x}/{z}.png
        List<TileEntry> tileEntries = new ArrayList<>();
        File[] worldDirs = tilesRoot.listFiles(File::isDirectory);
        if (worldDirs == null) return;

        for (File worldDir : worldDirs) {
            String worldName = worldDir.getName();
            File[] zoomDirs = worldDir.listFiles(File::isDirectory);
            if (zoomDirs == null) continue;

            for (File zoomDir : zoomDirs) {
                String zoom = zoomDir.getName();
                File[] xDirs = zoomDir.listFiles(File::isDirectory);
                if (xDirs == null) continue;

                for (File xDir : xDirs) {
                    String x = xDir.getName();
                    File[] pngFiles = xDir.listFiles((dir, name) -> name.endsWith(".png"));
                    if (pngFiles == null) continue;

                    for (File pngFile : pngFiles) {
                        String z = pngFile.getName().replace(".png", "");
                        tileEntries.add(new TileEntry(worldName, zoom, x, z, pngFile));
                    }
                }
            }
        }

        if (tileEntries.isEmpty()) {
            plugin.getLogger().info("[MapUpload] No tiles found to upload.");
            return;
        }

        plugin.getLogger().info("[MapUpload] Uploading " + tileEntries.size() + " tiles to " + uploadUrl + "...");

        int uploaded = 0;
        int failed = 0;

        // Send in batches
        for (int i = 0; i < tileEntries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, tileEntries.size());
            List<TileEntry> batch = tileEntries.subList(i, end);

            try {
                JsonObject payload = new JsonObject();
                JsonArray tilesArray = new JsonArray();

                for (TileEntry entry : batch) {
                    byte[] fileBytes = Files.readAllBytes(entry.file.toPath());
                    String base64 = Base64.getEncoder().encodeToString(fileBytes);

                    JsonObject tileObj = new JsonObject();
                    tileObj.addProperty("world", entry.world);
                    tileObj.addProperty("zoom", entry.zoom);
                    tileObj.addProperty("x", entry.x);
                    tileObj.addProperty("z", entry.z);
                    tileObj.addProperty("data", base64);
                    tilesArray.add(tileObj);
                }

                payload.add("tiles", tilesArray);
                String jsonStr = payload.toString();

                URL url = new URL(uploadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + syncSecret);
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode == 200) {
                    uploaded += batch.size();
                    plugin.getLogger().info("[MapUpload] Batch " + ((i / BATCH_SIZE) + 1) + " uploaded (" + uploaded + "/" + tileEntries.size() + ")");
                } else {
                    failed += batch.size();
                    plugin.getLogger().warning("[MapUpload] Batch " + ((i / BATCH_SIZE) + 1) + " failed with HTTP " + responseCode);
                }
            } catch (Exception e) {
                failed += batch.size();
                plugin.getLogger().log(Level.WARNING, "[MapUpload] Batch upload failed", e);
            }
        }

        plugin.getLogger().info("[MapUpload] Upload complete: " + uploaded + " succeeded, " + failed + " failed.");
    }

    private static class TileEntry {
        final String world;
        final String zoom;
        final String x;
        final String z;
        final File file;

        TileEntry(String world, String zoom, String x, String z, File file) {
            this.world = world;
            this.zoom = zoom;
            this.x = x;
            this.z = z;
            this.file = file;
        }
    }
}
