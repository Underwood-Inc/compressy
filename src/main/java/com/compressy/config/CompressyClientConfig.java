package com.compressy.config;

import com.compressy.CompressyMod;
import com.moandjiezana.toml.Toml;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side only configuration for Compressy mod.
 * 
 * This config is stored per-client and controls visual display preferences.
 * Settings here only affect what the client sees, not server behavior.
 * 
 * Config file location: config/compressy-client.toml
 */
public class CompressyClientConfig {
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("compressy-client.toml");
    
    private static CompressyClientConfig INSTANCE;
    
    // === CLIENT-SIDE CONFIG OPTIONS ===
    
    /**
     * Whether to show Roman numeral labels above placed compressed blocks.
     * This is a per-player (client-side) setting.
     * Only applies to FULL mode.
     * Default: true
     */
    public boolean showRomanNumerals = true;
    
    // === METHODS ===
    
    /**
     * Get the singleton client config instance.
     * Only works on the client side.
     */
    public static CompressyClientConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
    
    /**
     * Reload client config from disk.
     */
    public static void reload() {
        INSTANCE = null;
        load();
    }
    
    /**
     * Load config from file, or create default if not exists.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                Toml toml = new Toml().read(CONFIG_PATH.toFile());
                INSTANCE = new CompressyClientConfig();
                
                // Load boolean values
                INSTANCE.showRomanNumerals = toml.getBoolean("display.showRomanNumerals", true);
                
                CompressyMod.LOGGER.info("Loaded client config from {}", CONFIG_PATH);
                CompressyMod.LOGGER.info("  showRomanNumerals: {}", INSTANCE.showRomanNumerals);
            } catch (Exception e) {
                CompressyMod.LOGGER.error("Failed to load client config, using defaults", e);
                INSTANCE = new CompressyClientConfig();
            }
        } else {
            INSTANCE = new CompressyClientConfig();
            save(); // Create default config file
            CompressyMod.LOGGER.info("Created default client config at {}", CONFIG_PATH);
        }
    }
    
    /**
     * Save current client config to file.
     */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            
            // Build TOML file with helpful comments
            StringBuilder toml = new StringBuilder();
            toml.append("# Compressy Client Configuration File\n");
            toml.append("# This file controls per-player visual display preferences.\n");
            toml.append("# Each player can have their own settings - this is client-side only!\n\n");
            
            toml.append("# === DISPLAY SETTINGS ===\n");
            toml.append("# These settings only affect what YOU see, not other players.\n");
            toml.append("[display]\n");
            toml.append("# Show Roman numeral tier labels (I, II, III...) above placed compressed blocks.\n");
            toml.append("# Set to false to hide the tier display (only for you).\n");
            toml.append("showRomanNumerals = ").append(INSTANCE.showRomanNumerals).append("\n");
            
            Files.writeString(CONFIG_PATH, toml.toString());
            CompressyMod.LOGGER.info("Saved client config to {}", CONFIG_PATH);
            CompressyMod.LOGGER.info("  showRomanNumerals: {}", INSTANCE.showRomanNumerals);
        } catch (IOException e) {
            CompressyMod.LOGGER.error("Failed to save client config", e);
        }
    }
}

