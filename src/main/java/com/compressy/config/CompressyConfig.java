package com.compressy.config;

import com.compressy.CompressyMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for Compressy mod.
 * 
 * Supports:
 * - Block exclusions (blocks that cannot be compressed)
 * - Roman numeral label toggle (FULL mode only)
 * 
 * Config file location: config/compressy.json
 */
public class CompressyConfig {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("compressy.json");
    
    private static CompressyConfig INSTANCE;
    
    // === CONFIG OPTIONS ===
    
    /**
     * Whether to show Roman numeral labels above placed compressed blocks.
     * Only applies to FULL mode.
     * Default: true
     */
    public boolean showRomanNumerals = true;
    
    /**
     * Whether to show the darkening overlay on placed compressed blocks.
     * Only applies to FULL mode.
     * Default: true
     */
    public boolean showDarkeningOverlay = true;
    
    /**
     * List of block IDs that CANNOT be compressed.
     * Format: "minecraft:torch", "minecraft:flower_pot", etc.
     * 
     * Default exclusions are non-solid blocks that don't make sense to compress.
     */
    public List<String> excludedBlocks = new ArrayList<>();
    
    /**
     * Whether to use the default exclusion list.
     * If true, default exclusions are added to the excluded list.
     * Default: true
     */
    public boolean useDefaultExclusions = true;
    
    // === DEFAULT EXCLUSIONS ===
    // These are blocks that don't make sense to compress (non-solid, technical, etc.)
    
    private static final List<String> DEFAULT_EXCLUSIONS = List.of(
        // Technical blocks
        "minecraft:air",
        "minecraft:cave_air",
        "minecraft:void_air",
        "minecraft:barrier",
        "minecraft:structure_void",
        "minecraft:light",
        "minecraft:command_block",
        "minecraft:chain_command_block",
        "minecraft:repeating_command_block",
        "minecraft:structure_block",
        "minecraft:jigsaw",
        
        // Non-solid decorations (would lose placement context)
        "minecraft:torch",
        "minecraft:wall_torch",
        "minecraft:soul_torch",
        "minecraft:soul_wall_torch",
        "minecraft:redstone_torch",
        "minecraft:redstone_wall_torch",
        
        // Rails (orientation matters)
        "minecraft:rail",
        "minecraft:powered_rail",
        "minecraft:detector_rail",
        "minecraft:activator_rail",
        
        // Redstone components
        "minecraft:redstone_wire",
        "minecraft:lever",
        "minecraft:tripwire",
        "minecraft:tripwire_hook",
        
        // Beds (multi-block)
        "minecraft:white_bed",
        "minecraft:orange_bed",
        "minecraft:magenta_bed",
        "minecraft:light_blue_bed",
        "minecraft:yellow_bed",
        "minecraft:lime_bed",
        "minecraft:pink_bed",
        "minecraft:gray_bed",
        "minecraft:light_gray_bed",
        "minecraft:cyan_bed",
        "minecraft:purple_bed",
        "minecraft:blue_bed",
        "minecraft:brown_bed",
        "minecraft:green_bed",
        "minecraft:red_bed",
        "minecraft:black_bed",
        
        // Doors (multi-block)
        "minecraft:oak_door",
        "minecraft:spruce_door",
        "minecraft:birch_door",
        "minecraft:jungle_door",
        "minecraft:acacia_door",
        "minecraft:dark_oak_door",
        "minecraft:mangrove_door",
        "minecraft:cherry_door",
        "minecraft:bamboo_door",
        "minecraft:crimson_door",
        "minecraft:warped_door",
        "minecraft:iron_door",
        "minecraft:copper_door",
        "minecraft:exposed_copper_door",
        "minecraft:weathered_copper_door",
        "minecraft:oxidized_copper_door",
        "minecraft:waxed_copper_door",
        "minecraft:waxed_exposed_copper_door",
        "minecraft:waxed_weathered_copper_door",
        "minecraft:waxed_oxidized_copper_door"
    );
    
    // === METHODS ===
    
    /**
     * Get the singleton config instance.
     */
    public static CompressyConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
    
    /**
     * Load config from file, or create default if not exists.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, CompressyConfig.class);
                CompressyMod.LOGGER.info("Loaded config from {}", CONFIG_PATH);
            } catch (IOException e) {
                CompressyMod.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new CompressyConfig();
            }
        } else {
            INSTANCE = new CompressyConfig();
            save(); // Create default config file
            CompressyMod.LOGGER.info("Created default config at {}", CONFIG_PATH);
        }
    }
    
    /**
     * Save current config to file.
     */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
            CompressyMod.LOGGER.info("Saved config to {}", CONFIG_PATH);
        } catch (IOException e) {
            CompressyMod.LOGGER.error("Failed to save config", e);
        }
    }
    
    /**
     * Check if a block ID is excluded from compression.
     * 
     * @param blockId The block ID (e.g., "minecraft:stone")
     * @return true if the block should NOT be compressible
     */
    public boolean isBlockExcluded(String blockId) {
        // Check user exclusions
        if (excludedBlocks.contains(blockId)) {
            return true;
        }
        
        // Check default exclusions if enabled
        if (useDefaultExclusions && DEFAULT_EXCLUSIONS.contains(blockId)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all currently active exclusions (for display in config screen).
     */
    public List<String> getAllActiveExclusions() {
        List<String> all = new ArrayList<>(excludedBlocks);
        if (useDefaultExclusions) {
            for (String def : DEFAULT_EXCLUSIONS) {
                if (!all.contains(def)) {
                    all.add(def);
                }
            }
        }
        return all;
    }
    
    /**
     * Get the default exclusions list (for reference).
     */
    public static List<String> getDefaultExclusions() {
        return DEFAULT_EXCLUSIONS;
    }
}

