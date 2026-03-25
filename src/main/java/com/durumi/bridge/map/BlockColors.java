package com.durumi.bridge.map;

import org.bukkit.Material;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

/**
 * Maps Minecraft block types to their representative top-down map colors.
 * Contains 100+ block mappings for accurate map rendering.
 */
public final class BlockColors {

    private static final Map<Material, Color> COLORS = new EnumMap<>(Material.class);

    static {
        // Grass & dirt variants
        COLORS.put(Material.GRASS_BLOCK, new Color(124, 189, 73));
        COLORS.put(Material.DIRT, new Color(134, 96, 67));
        COLORS.put(Material.COARSE_DIRT, new Color(119, 85, 59));
        COLORS.put(Material.ROOTED_DIRT, new Color(115, 82, 56));
        COLORS.put(Material.DIRT_PATH, new Color(148, 121, 65));
        COLORS.put(Material.FARMLAND, new Color(110, 75, 44));
        COLORS.put(Material.PODZOL, new Color(91, 63, 24));
        COLORS.put(Material.MYCELIUM, new Color(111, 99, 107));
        COLORS.put(Material.MUD, new Color(60, 56, 62));
        COLORS.put(Material.MUDDY_MANGROVE_ROOTS, new Color(68, 58, 43));
        COLORS.put(Material.MOSS_BLOCK, new Color(89, 109, 45));

        // Stone variants
        COLORS.put(Material.STONE, new Color(125, 125, 125));
        COLORS.put(Material.COBBLESTONE, new Color(127, 127, 127));
        COLORS.put(Material.MOSSY_COBBLESTONE, new Color(110, 127, 95));
        COLORS.put(Material.STONE_BRICKS, new Color(122, 122, 122));
        COLORS.put(Material.MOSSY_STONE_BRICKS, new Color(104, 121, 89));
        COLORS.put(Material.CRACKED_STONE_BRICKS, new Color(118, 117, 118));
        COLORS.put(Material.SMOOTH_STONE, new Color(158, 158, 158));
        COLORS.put(Material.GRANITE, new Color(149, 103, 85));
        COLORS.put(Material.POLISHED_GRANITE, new Color(154, 106, 89));
        COLORS.put(Material.DIORITE, new Color(188, 182, 183));
        COLORS.put(Material.POLISHED_DIORITE, new Color(192, 193, 194));
        COLORS.put(Material.ANDESITE, new Color(136, 136, 136));
        COLORS.put(Material.POLISHED_ANDESITE, new Color(132, 135, 133));
        COLORS.put(Material.DEEPSLATE, new Color(80, 80, 84));
        COLORS.put(Material.COBBLED_DEEPSLATE, new Color(77, 77, 80));
        COLORS.put(Material.TUFF, new Color(108, 109, 102));
        COLORS.put(Material.CALCITE, new Color(223, 224, 220));
        COLORS.put(Material.DRIPSTONE_BLOCK, new Color(134, 107, 92));
        COLORS.put(Material.BEDROCK, new Color(85, 85, 85));

        // Sand variants
        COLORS.put(Material.SAND, new Color(219, 207, 163));
        COLORS.put(Material.RED_SAND, new Color(190, 102, 33));
        COLORS.put(Material.SANDSTONE, new Color(216, 203, 155));
        COLORS.put(Material.RED_SANDSTONE, new Color(181, 97, 31));
        COLORS.put(Material.GRAVEL, new Color(131, 127, 126));
        COLORS.put(Material.CLAY, new Color(160, 166, 179));
        COLORS.put(Material.SOUL_SAND, new Color(81, 62, 50));
        COLORS.put(Material.SOUL_SOIL, new Color(75, 57, 46));

        // Water & ice
        COLORS.put(Material.WATER, new Color(63, 118, 228));
        COLORS.put(Material.ICE, new Color(145, 183, 253));
        COLORS.put(Material.PACKED_ICE, new Color(141, 180, 250));
        COLORS.put(Material.BLUE_ICE, new Color(116, 167, 253));
        COLORS.put(Material.FROSTED_ICE, new Color(140, 181, 252));
        COLORS.put(Material.SNOW_BLOCK, new Color(249, 254, 254));
        COLORS.put(Material.SNOW, new Color(249, 254, 254));
        COLORS.put(Material.POWDER_SNOW, new Color(248, 253, 253));

        // Wood - oak
        COLORS.put(Material.OAK_LOG, new Color(109, 85, 50));
        COLORS.put(Material.OAK_WOOD, new Color(109, 85, 50));
        COLORS.put(Material.OAK_PLANKS, new Color(162, 130, 78));
        COLORS.put(Material.STRIPPED_OAK_LOG, new Color(177, 144, 86));
        COLORS.put(Material.OAK_LEAVES, new Color(59, 122, 22));

        // Wood - spruce
        COLORS.put(Material.SPRUCE_LOG, new Color(58, 37, 16));
        COLORS.put(Material.SPRUCE_WOOD, new Color(58, 37, 16));
        COLORS.put(Material.SPRUCE_PLANKS, new Color(114, 84, 48));
        COLORS.put(Material.STRIPPED_SPRUCE_LOG, new Color(115, 89, 52));
        COLORS.put(Material.SPRUCE_LEAVES, new Color(57, 90, 57));

        // Wood - birch
        COLORS.put(Material.BIRCH_LOG, new Color(216, 215, 210));
        COLORS.put(Material.BIRCH_WOOD, new Color(216, 215, 210));
        COLORS.put(Material.BIRCH_PLANKS, new Color(196, 179, 123));
        COLORS.put(Material.STRIPPED_BIRCH_LOG, new Color(196, 176, 118));
        COLORS.put(Material.BIRCH_LEAVES, new Color(75, 128, 41));

        // Wood - jungle
        COLORS.put(Material.JUNGLE_LOG, new Color(85, 67, 25));
        COLORS.put(Material.JUNGLE_WOOD, new Color(85, 67, 25));
        COLORS.put(Material.JUNGLE_PLANKS, new Color(160, 115, 80));
        COLORS.put(Material.STRIPPED_JUNGLE_LOG, new Color(171, 132, 84));
        COLORS.put(Material.JUNGLE_LEAVES, new Color(42, 107, 14));

        // Wood - acacia
        COLORS.put(Material.ACACIA_LOG, new Color(103, 96, 86));
        COLORS.put(Material.ACACIA_WOOD, new Color(103, 96, 86));
        COLORS.put(Material.ACACIA_PLANKS, new Color(168, 90, 50));
        COLORS.put(Material.STRIPPED_ACACIA_LOG, new Color(174, 92, 59));
        COLORS.put(Material.ACACIA_LEAVES, new Color(58, 112, 10));

        // Wood - dark oak
        COLORS.put(Material.DARK_OAK_LOG, new Color(60, 46, 26));
        COLORS.put(Material.DARK_OAK_WOOD, new Color(60, 46, 26));
        COLORS.put(Material.DARK_OAK_PLANKS, new Color(66, 43, 20));
        COLORS.put(Material.STRIPPED_DARK_OAK_LOG, new Color(96, 76, 49));
        COLORS.put(Material.DARK_OAK_LEAVES, new Color(37, 81, 9));

        // Wood - mangrove
        COLORS.put(Material.MANGROVE_LOG, new Color(84, 56, 31));
        COLORS.put(Material.MANGROVE_WOOD, new Color(84, 56, 31));
        COLORS.put(Material.MANGROVE_PLANKS, new Color(117, 54, 48));
        COLORS.put(Material.MANGROVE_LEAVES, new Color(57, 107, 24));

        // Wood - cherry
        COLORS.put(Material.CHERRY_LOG, new Color(53, 25, 33));
        COLORS.put(Material.CHERRY_WOOD, new Color(53, 25, 33));
        COLORS.put(Material.CHERRY_PLANKS, new Color(226, 178, 172));
        COLORS.put(Material.CHERRY_LEAVES, new Color(233, 170, 192));

        // Wood - bamboo
        COLORS.put(Material.BAMBOO_BLOCK, new Color(126, 141, 35));
        COLORS.put(Material.BAMBOO_PLANKS, new Color(194, 173, 82));
        COLORS.put(Material.BAMBOO_MOSAIC, new Color(190, 170, 78));

        // Wood - crimson & warped (nether)
        COLORS.put(Material.CRIMSON_STEM, new Color(92, 25, 29));
        COLORS.put(Material.CRIMSON_PLANKS, new Color(101, 48, 70));
        COLORS.put(Material.WARPED_STEM, new Color(26, 70, 71));
        COLORS.put(Material.WARPED_PLANKS, new Color(43, 104, 99));
        COLORS.put(Material.CRIMSON_NYLIUM, new Color(130, 31, 31));
        COLORS.put(Material.WARPED_NYLIUM, new Color(21, 119, 121));
        COLORS.put(Material.NETHER_WART_BLOCK, new Color(114, 2, 2));
        COLORS.put(Material.WARPED_WART_BLOCK, new Color(22, 119, 121));

        // Nether blocks
        COLORS.put(Material.NETHERRACK, new Color(97, 38, 38));
        COLORS.put(Material.NETHER_BRICKS, new Color(44, 21, 26));
        COLORS.put(Material.BASALT, new Color(72, 72, 78));
        COLORS.put(Material.SMOOTH_BASALT, new Color(72, 72, 72));
        COLORS.put(Material.BLACKSTONE, new Color(42, 36, 41));
        COLORS.put(Material.GLOWSTONE, new Color(171, 131, 84));
        COLORS.put(Material.MAGMA_BLOCK, new Color(142, 63, 31));
        COLORS.put(Material.OBSIDIAN, new Color(15, 10, 24));
        COLORS.put(Material.CRYING_OBSIDIAN, new Color(32, 10, 60));

        // End blocks
        COLORS.put(Material.END_STONE, new Color(219, 222, 158));
        COLORS.put(Material.END_STONE_BRICKS, new Color(218, 224, 162));
        COLORS.put(Material.PURPUR_BLOCK, new Color(169, 125, 169));

        // Ores
        COLORS.put(Material.COAL_ORE, new Color(105, 105, 105));
        COLORS.put(Material.IRON_ORE, new Color(136, 129, 122));
        COLORS.put(Material.GOLD_ORE, new Color(143, 140, 125));
        COLORS.put(Material.DIAMOND_ORE, new Color(121, 141, 140));
        COLORS.put(Material.EMERALD_ORE, new Color(108, 136, 115));
        COLORS.put(Material.LAPIS_ORE, new Color(99, 111, 133));
        COLORS.put(Material.REDSTONE_ORE, new Color(133, 107, 107));
        COLORS.put(Material.COPPER_ORE, new Color(124, 125, 112));

        // Ore blocks
        COLORS.put(Material.IRON_BLOCK, new Color(220, 220, 220));
        COLORS.put(Material.GOLD_BLOCK, new Color(246, 208, 61));
        COLORS.put(Material.DIAMOND_BLOCK, new Color(98, 237, 228));
        COLORS.put(Material.EMERALD_BLOCK, new Color(42, 176, 67));
        COLORS.put(Material.LAPIS_BLOCK, new Color(31, 67, 140));
        COLORS.put(Material.REDSTONE_BLOCK, new Color(171, 26, 4));
        COLORS.put(Material.COAL_BLOCK, new Color(16, 15, 15));
        COLORS.put(Material.COPPER_BLOCK, new Color(192, 107, 79));
        COLORS.put(Material.RAW_IRON_BLOCK, new Color(166, 135, 107));
        COLORS.put(Material.RAW_GOLD_BLOCK, new Color(221, 169, 46));
        COLORS.put(Material.RAW_COPPER_BLOCK, new Color(154, 105, 79));
        COLORS.put(Material.AMETHYST_BLOCK, new Color(133, 97, 191));
        COLORS.put(Material.NETHERITE_BLOCK, new Color(66, 61, 63));

        // Terracotta
        COLORS.put(Material.TERRACOTTA, new Color(152, 94, 67));
        COLORS.put(Material.WHITE_TERRACOTTA, new Color(209, 178, 161));
        COLORS.put(Material.ORANGE_TERRACOTTA, new Color(161, 83, 37));
        COLORS.put(Material.MAGENTA_TERRACOTTA, new Color(149, 88, 108));
        COLORS.put(Material.LIGHT_BLUE_TERRACOTTA, new Color(113, 108, 137));
        COLORS.put(Material.YELLOW_TERRACOTTA, new Color(186, 133, 35));
        COLORS.put(Material.LIME_TERRACOTTA, new Color(103, 117, 52));
        COLORS.put(Material.PINK_TERRACOTTA, new Color(161, 78, 78));
        COLORS.put(Material.GRAY_TERRACOTTA, new Color(57, 42, 35));
        COLORS.put(Material.LIGHT_GRAY_TERRACOTTA, new Color(135, 106, 97));
        COLORS.put(Material.CYAN_TERRACOTTA, new Color(86, 91, 91));
        COLORS.put(Material.PURPLE_TERRACOTTA, new Color(118, 70, 86));
        COLORS.put(Material.BLUE_TERRACOTTA, new Color(74, 59, 91));
        COLORS.put(Material.BROWN_TERRACOTTA, new Color(77, 51, 35));
        COLORS.put(Material.GREEN_TERRACOTTA, new Color(76, 83, 42));
        COLORS.put(Material.RED_TERRACOTTA, new Color(143, 61, 46));
        COLORS.put(Material.BLACK_TERRACOTTA, new Color(37, 22, 16));

        // Concrete
        COLORS.put(Material.WHITE_CONCRETE, new Color(207, 213, 214));
        COLORS.put(Material.ORANGE_CONCRETE, new Color(224, 97, 0));
        COLORS.put(Material.MAGENTA_CONCRETE, new Color(169, 48, 159));
        COLORS.put(Material.LIGHT_BLUE_CONCRETE, new Color(35, 137, 198));
        COLORS.put(Material.YELLOW_CONCRETE, new Color(240, 175, 21));
        COLORS.put(Material.LIME_CONCRETE, new Color(94, 168, 24));
        COLORS.put(Material.PINK_CONCRETE, new Color(213, 101, 142));
        COLORS.put(Material.GRAY_CONCRETE, new Color(54, 57, 61));
        COLORS.put(Material.LIGHT_GRAY_CONCRETE, new Color(125, 125, 115));
        COLORS.put(Material.CYAN_CONCRETE, new Color(21, 119, 136));
        COLORS.put(Material.PURPLE_CONCRETE, new Color(100, 31, 156));
        COLORS.put(Material.BLUE_CONCRETE, new Color(44, 46, 143));
        COLORS.put(Material.BROWN_CONCRETE, new Color(96, 59, 31));
        COLORS.put(Material.GREEN_CONCRETE, new Color(73, 91, 36));
        COLORS.put(Material.RED_CONCRETE, new Color(142, 32, 32));
        COLORS.put(Material.BLACK_CONCRETE, new Color(8, 10, 15));

        // Wool
        COLORS.put(Material.WHITE_WOOL, new Color(233, 236, 236));
        COLORS.put(Material.ORANGE_WOOL, new Color(240, 118, 19));
        COLORS.put(Material.MAGENTA_WOOL, new Color(189, 68, 179));
        COLORS.put(Material.LIGHT_BLUE_WOOL, new Color(58, 175, 217));
        COLORS.put(Material.YELLOW_WOOL, new Color(248, 197, 39));
        COLORS.put(Material.LIME_WOOL, new Color(112, 185, 25));
        COLORS.put(Material.PINK_WOOL, new Color(237, 141, 172));
        COLORS.put(Material.GRAY_WOOL, new Color(62, 68, 71));
        COLORS.put(Material.LIGHT_GRAY_WOOL, new Color(142, 142, 134));
        COLORS.put(Material.CYAN_WOOL, new Color(21, 137, 145));
        COLORS.put(Material.PURPLE_WOOL, new Color(121, 42, 172));
        COLORS.put(Material.BLUE_WOOL, new Color(53, 57, 157));
        COLORS.put(Material.BROWN_WOOL, new Color(114, 71, 40));
        COLORS.put(Material.GREEN_WOOL, new Color(84, 109, 27));
        COLORS.put(Material.RED_WOOL, new Color(160, 39, 34));
        COLORS.put(Material.BLACK_WOOL, new Color(20, 21, 25));

        // Glass
        COLORS.put(Material.GLASS, new Color(175, 213, 219));
        COLORS.put(Material.TINTED_GLASS, new Color(43, 30, 41));

        // Misc building blocks
        COLORS.put(Material.BRICKS, new Color(150, 97, 83));
        COLORS.put(Material.BOOKSHELF, new Color(109, 87, 55));
        COLORS.put(Material.PRISMARINE, new Color(99, 171, 158));
        COLORS.put(Material.DARK_PRISMARINE, new Color(51, 91, 75));
        COLORS.put(Material.SEA_LANTERN, new Color(172, 199, 190));
        COLORS.put(Material.SPONGE, new Color(195, 192, 74));
        COLORS.put(Material.MELON, new Color(111, 145, 30));
        COLORS.put(Material.PUMPKIN, new Color(198, 118, 24));
        COLORS.put(Material.HAY_BLOCK, new Color(166, 139, 12));
        COLORS.put(Material.HONEY_BLOCK, new Color(235, 166, 42));
        COLORS.put(Material.HONEYCOMB_BLOCK, new Color(229, 148, 29));
        COLORS.put(Material.SLIME_BLOCK, new Color(111, 192, 73));
        COLORS.put(Material.BONE_BLOCK, new Color(229, 225, 207));
        COLORS.put(Material.TNT, new Color(186, 53, 40));
        COLORS.put(Material.SCULK, new Color(12, 29, 36));
        COLORS.put(Material.SCULK_CATALYST, new Color(15, 36, 40));
        COLORS.put(Material.REINFORCED_DEEPSLATE, new Color(80, 82, 78));

        // Plants / flowers (short, but still visible from top)
        COLORS.put(Material.SHORT_GRASS, new Color(110, 172, 58));
        COLORS.put(Material.TALL_GRASS, new Color(110, 172, 58));
        COLORS.put(Material.FERN, new Color(90, 148, 45));
        COLORS.put(Material.LARGE_FERN, new Color(90, 148, 45));
        COLORS.put(Material.DEAD_BUSH, new Color(109, 79, 37));
        COLORS.put(Material.LILY_PAD, new Color(33, 107, 18));
        COLORS.put(Material.VINE, new Color(64, 115, 32));
        COLORS.put(Material.SUGAR_CANE, new Color(148, 192, 101));
        COLORS.put(Material.CACTUS, new Color(85, 127, 43));
        COLORS.put(Material.BAMBOO, new Color(93, 126, 26));
        COLORS.put(Material.KELP, new Color(86, 130, 41));
        COLORS.put(Material.SEAGRASS, new Color(44, 122, 13));

        // Lava
        COLORS.put(Material.LAVA, new Color(207, 85, 16));

        // Miscellaneous
        COLORS.put(Material.CRAFTING_TABLE, new Color(120, 80, 42));
        COLORS.put(Material.FURNACE, new Color(120, 120, 120));
        COLORS.put(Material.CHEST, new Color(162, 130, 78));
        COLORS.put(Material.BARREL, new Color(107, 80, 46));
    }

    private BlockColors() {
    }

    /**
     * Get the map color for a given material.
     *
     * @param material the block material
     * @return the Color, or null if no mapping exists
     */
    public static Color getColor(Material material) {
        return COLORS.get(material);
    }

    /**
     * Get the map color for a given material, with a fallback.
     *
     * @param material the block material
     * @param fallback the fallback color if no mapping exists
     * @return the Color
     */
    public static Color getColorOrDefault(Material material, Color fallback) {
        return COLORS.getOrDefault(material, fallback);
    }

    /**
     * Check if a block material has a color mapping.
     */
    public static boolean hasColor(Material material) {
        return COLORS.containsKey(material);
    }

    /**
     * Get the total number of mapped block colors.
     */
    public static int size() {
        return COLORS.size();
    }
}
