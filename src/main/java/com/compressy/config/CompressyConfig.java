package com.compressy.config;

import com.compressy.CompressyMod;
import com.moandjiezana.toml.Toml;

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
 * Config file location: config/compressy.toml
 * Uses TOML format to support comments for user guidance!
 */
public class CompressyConfig {
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("compressy.toml");
    
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
    
    /**
     * Whether to use allowlist mode.
     * If true, ONLY blocks in allowedBlocks can be compressed (exclusions are ignored).
     * If false, all blocks can be compressed EXCEPT those in excludedBlocks.
     * Default: false
     */
    public boolean useAllowlist = false;
    
    /**
     * List of block IDs that CAN be compressed (when useAllowlist is true).
     * Format: "minecraft:stone", "minecraft:dirt", etc.
     * 
     * When useAllowlist is false, this list is ignored.
     */
    public List<String> allowedBlocks = new ArrayList<>();
    
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
     * Reload config from disk (useful after external edits).
     */
    public static void reload() {
        INSTANCE = null; // Force reload
        load();
    }
    
    /**
     * Load config from file, or create default if not exists.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                Toml toml = new Toml().read(CONFIG_PATH.toFile());
                INSTANCE = new CompressyConfig();
                
                // Load boolean values
                INSTANCE.showRomanNumerals = toml.getBoolean("display.showRomanNumerals", true);
                INSTANCE.showDarkeningOverlay = toml.getBoolean("display.showDarkeningOverlay", true);
                INSTANCE.useDefaultExclusions = toml.getBoolean("blocks.useDefaultExclusions", true);
                INSTANCE.useAllowlist = toml.getBoolean("blocks.useAllowlist", false);
                
                // Load lists
                List<Object> excluded = toml.getList("blocks.excludedBlocks");
                if (excluded != null) {
                    INSTANCE.excludedBlocks = new ArrayList<>();
                    for (Object item : excluded) {
                        if (item != null) {
                            INSTANCE.excludedBlocks.add(item.toString());
                        }
                    }
                } else {
                    INSTANCE.excludedBlocks = new ArrayList<>();
                }
                
                List<Object> allowed = toml.getList("blocks.allowedBlocks");
                if (allowed != null) {
                    INSTANCE.allowedBlocks = new ArrayList<>();
                    for (Object item : allowed) {
                        if (item != null) {
                            INSTANCE.allowedBlocks.add(item.toString());
                        }
                    }
                } else {
                    INSTANCE.allowedBlocks = new ArrayList<>();
                }
                
                CompressyMod.LOGGER.info("Loaded config from {}", CONFIG_PATH);
                CompressyMod.LOGGER.info("  showRomanNumerals: {}", INSTANCE.showRomanNumerals);
                CompressyMod.LOGGER.info("  showDarkeningOverlay: {}", INSTANCE.showDarkeningOverlay);
            } catch (Exception e) {
                CompressyMod.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new CompressyConfig();
            }
        } else {
            INSTANCE = new CompressyConfig();
            save(); // Create default config file with comments
            CompressyMod.LOGGER.info("Created default config at {}", CONFIG_PATH);
        }
    }
    
    /**
     * Save current config to file with helpful comments.
     */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            
            // Build TOML file with helpful comments
            StringBuilder toml = new StringBuilder();
            toml.append("# Compressy Configuration File\n");
            toml.append("# This file uses TOML format - you can add comments like this!\n");
            toml.append("# Edit this file directly or use ModMenu in-game.\n\n");
            
            toml.append("# === DISPLAY SETTINGS ===\n");
            toml.append("# These settings only apply to FULL mode (not LITE mode).\n");
            toml.append("[display]\n");
            toml.append("# Show Roman numeral tier labels (I, II, III...) above placed compressed blocks.\n");
            toml.append("# Set to false to hide the tier display.\n");
            toml.append("showRomanNumerals = ").append(INSTANCE.showRomanNumerals).append("\n\n");
            toml.append("# Show darkening overlay effect on higher tier compressed blocks.\n");
            toml.append("# Higher compression levels get progressively darker overlays.\n");
            toml.append("showDarkeningOverlay = ").append(INSTANCE.showDarkeningOverlay).append("\n\n");
            
            toml.append("# === BLOCK MANAGEMENT ===\n");
            toml.append("# Control which blocks can be compressed.\n");
            toml.append("[blocks]\n");
            toml.append("# Allowlist mode: If true, ONLY blocks in allowedBlocks can be compressed.\n");
            toml.append("# If false, all blocks can be compressed EXCEPT those in excludedBlocks.\n");
            toml.append("useAllowlist = ").append(INSTANCE.useAllowlist).append("\n\n");
            toml.append("# Use default exclusions: Automatically exclude technical/non-solid blocks.\n");
            toml.append("# This includes air, torches, rails, beds, doors, etc.\n");
            toml.append("# Only applies when useAllowlist is false.\n");
            toml.append("useDefaultExclusions = ").append(INSTANCE.useDefaultExclusions).append("\n\n");
            toml.append("# Excluded blocks: Blocks that CANNOT be compressed (when useAllowlist is false).\n");
            toml.append("# Format: block IDs like \"minecraft:torch\" or \"modid:blockname\"\n");
            toml.append("# Example: excludedBlocks = [\"minecraft:bedrock\", \"minecraft:command_block\"]\n");
            toml.append("excludedBlocks = ").append(formatList(INSTANCE.excludedBlocks)).append("\n\n");
            toml.append("# Allowed blocks: Blocks that CAN be compressed (when useAllowlist is true).\n");
            toml.append("# Format: block IDs like \"minecraft:stone\" or \"modid:blockname\"\n");
            toml.append("# Example: allowedBlocks = [\"minecraft:stone\", \"minecraft:dirt\", \"minecraft:cobblestone\"]\n");
            toml.append("allowedBlocks = ").append(formatList(INSTANCE.allowedBlocks)).append("\n");
            
            Files.writeString(CONFIG_PATH, toml.toString());
            CompressyMod.LOGGER.info("Saved config to {}", CONFIG_PATH);
            CompressyMod.LOGGER.info("  showRomanNumerals: {}", INSTANCE.showRomanNumerals);
            CompressyMod.LOGGER.info("  showDarkeningOverlay: {}", INSTANCE.showDarkeningOverlay);
        } catch (IOException e) {
            CompressyMod.LOGGER.error("Failed to save config", e);
        }
    }
    
    /**
     * Format a list for TOML output.
     */
    private static String formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Check if a block ID is excluded from compression.
     * 
     * @param blockId The block ID (e.g., "minecraft:stone")
     * @return true if the block should NOT be compressible
     */
    public boolean isBlockExcluded(String blockId) {
        // If allowlist mode is enabled, check if block is in allowlist
        if (useAllowlist) {
            return !allowedBlocks.contains(blockId);
        }
        
        // Exclusion mode: check user exclusions
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
     * Check if a block ID is allowed to be compressed.
     * 
     * @param blockId The block ID (e.g., "minecraft:stone")
     * @return true if the block CAN be compressed
     */
    public boolean isBlockAllowed(String blockId) {
        return !isBlockExcluded(blockId);
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

