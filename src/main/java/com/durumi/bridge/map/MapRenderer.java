package com.durumi.bridge.map;

import com.durumi.bridge.DurumiBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Renders top-down map tiles for Leaflet.js consumption.
 * Tiles are 256x256 pixels in z/x/y.png format.
 */
public class MapRenderer {

    private static final int TILE_SIZE = 256;
    private static final Color DEFAULT_COLOR = new Color(0, 0, 0, 0);
    private static final Color VOID_COLOR = new Color(20, 20, 30);

    private final DurumiBridge plugin;
    private final File tilesRoot;

    public MapRenderer(DurumiBridge plugin) {
        this.plugin = plugin;
        this.tilesRoot = new File(plugin.getDataFolder(), "tiles");
        if (!tilesRoot.exists()) {
            tilesRoot.mkdirs();
        }
    }

    public File getTilesRoot() {
        return tilesRoot;
    }

    /**
     * Render all tiles for a world within the given radius (in chunks) from spawn.
     * This runs the block data collection on the main thread (required for chunk access)
     * and the image writing on an async thread.
     */
    public void renderWorld(String worldName, int radiusChunks) {
        renderWorld(worldName, radiusChunks, null);
    }

    /**
     * Render all tiles for a world within the given radius (in chunks) from spawn.
     * When all tiles are written, the onComplete callback is invoked (on an async thread).
     */
    public void renderWorld(String worldName, int radiusChunks, Runnable onComplete) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Cannot render map for unknown world: " + worldName);
            if (onComplete != null) onComplete.run();
            return;
        }

        plugin.getLogger().info("Starting map render for world '" + worldName + "' with radius " + radiusChunks + " chunks...");

        Location spawn = world.getSpawnLocation();
        int spawnChunkX = spawn.getBlockX() >> 4;
        int spawnChunkZ = spawn.getBlockZ() >> 4;

        // We render at zoom level 0 where 1 tile = 16x16 chunks (256x256 blocks)
        // Each tile covers 256 blocks, and each chunk is 16 blocks, so 1 tile = 16 chunks
        int tilesPerSide = (int) Math.ceil((double) radiusChunks / 16.0);

        int centerTileX = spawnChunkX >> 4; // divide by 16 to get tile coordinate
        int centerTileZ = spawnChunkZ >> 4;

        int totalTiles = (2 * tilesPerSide + 1) * (2 * tilesPerSide + 1);
        plugin.getLogger().info("Rendering " + totalTiles + " tiles for world '" + worldName + "'...");

        AtomicInteger remaining = new AtomicInteger(totalTiles);

        for (int tileX = centerTileX - tilesPerSide; tileX <= centerTileX + tilesPerSide; tileX++) {
            for (int tileZ = centerTileZ - tilesPerSide; tileZ <= centerTileZ + tilesPerSide; tileZ++) {
                final int tx = tileX;
                final int tz = tileZ;
                renderTile(world, worldName, tx, tz, () -> {
                    if (remaining.decrementAndGet() == 0) {
                        plugin.getLogger().info("Map render complete for world '" + worldName + "'.");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
            }
        }
    }

    /**
     * Render a single 256x256 tile. Each pixel = 1 block.
     * Tile coordinates define which 256x256 block area to render.
     */
    private void renderTile(World world, String worldName, int tileX, int tileZ, Runnable onTileDone) {
        // Calculate the block origin for this tile
        int blockStartX = tileX * TILE_SIZE;
        int blockStartZ = tileZ * TILE_SIZE;

        // We must read blocks on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            int[] rgbData = new int[TILE_SIZE * TILE_SIZE];

            for (int px = 0; px < TILE_SIZE; px++) {
                for (int pz = 0; pz < TILE_SIZE; pz++) {
                    int blockX = blockStartX + px;
                    int blockZ = blockStartZ + pz;

                    Color color = getTopBlockColor(world, blockX, blockZ);
                    rgbData[pz * TILE_SIZE + px] = color.getRGB();
                }
            }

            // Write the image asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    BufferedImage image = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
                    image.setRGB(0, 0, TILE_SIZE, TILE_SIZE, rgbData, 0, TILE_SIZE);

                    // Save as tiles/{world}/0/{x}/{z}.png
                    File dir = new File(tilesRoot, worldName + "/0/" + tileX);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, tileZ + ".png");
                    ImageIO.write(image, "PNG", file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING,
                            "Failed to write tile " + tileX + "/" + tileZ + " for world " + worldName, e);
                } finally {
                    if (onTileDone != null) {
                        onTileDone.run();
                    }
                }
            });
        });
    }

    /**
     * Get the color of the highest non-air block at the given x/z position.
     * Applies simple height-based shading for depth effect.
     */
    private Color getTopBlockColor(World world, int x, int z) {
        int maxY = world.getMaxHeight() - 1;
        int minY = world.getMinHeight();

        // Check if the chunk is loaded; skip unloaded chunks
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return VOID_COLOR;
        }

        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Material material = block.getType();

            if (material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR) {
                continue;
            }

            // Skip transparent non-solid blocks we don't have colors for
            Color baseColor = BlockColors.getColor(material);
            if (baseColor != null) {
                return applyHeightShading(baseColor, y, world.getSeaLevel());
            }

            // For unmapped solid blocks, use a gray fallback
            if (material.isSolid()) {
                return applyHeightShading(new Color(128, 128, 128), y, world.getSeaLevel());
            }
        }

        return VOID_COLOR;
    }

    /**
     * Apply simple height-based brightness shading.
     * Blocks above sea level are slightly brighter, below are slightly darker.
     */
    private Color applyHeightShading(Color base, int y, int seaLevel) {
        float factor = 1.0f + (y - seaLevel) * 0.003f;
        factor = Math.max(0.6f, Math.min(1.4f, factor));

        int r = Math.min(255, Math.max(0, (int) (base.getRed() * factor)));
        int g = Math.min(255, Math.max(0, (int) (base.getGreen() * factor)));
        int b = Math.min(255, Math.max(0, (int) (base.getBlue() * factor)));

        return new Color(r, g, b);
    }

    /**
     * Get the file for a specific tile, or null if it doesn't exist.
     */
    public File getTileFile(String worldName, int zoom, int x, int y) {
        File file = new File(tilesRoot, worldName + "/" + zoom + "/" + x + "/" + y + ".png");
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
