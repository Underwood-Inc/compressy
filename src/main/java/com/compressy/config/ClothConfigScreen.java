package com.compressy.config;

import com.compressy.CompressyMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

/**
 * Cloth Config-based config screen for Compressy.
 * Only loaded if Cloth Config is available.
 */
public class ClothConfigScreen {
    
    /**
     * Create the config screen.
     * This method is called via reflection/try-catch to handle missing Cloth Config.
     */
    public static Screen create(Screen parent) {
        // Get the current config instance - this will be modified by save consumers
        CompressyConfig config = CompressyConfig.get();
        
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Compressy Configuration"));
        
        // Save and log when done
        builder.setSavingRunnable(() -> {
            CompressyConfig.save();
            CompressyMod.LOGGER.info("Config saved from ModMenu - values should now be active");
        });
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        // === INFO CATEGORY ===
        ConfigCategory info = builder.getOrCreateCategory(Text.literal("Information"));
        
        // Show current mode and statistics
        int totalExcluded = config.getAllActiveExclusions().size();
        int userExcluded = config.excludedBlocks.size();
        int defaultExcluded = config.useDefaultExclusions ? CompressyConfig.getDefaultExclusions().size() : 0;
        int allowed = config.allowedBlocks.size();
        
        String modeText = config.useAllowlist 
            ? "Allowlist Mode (only " + allowed + " block(s) can be compressed)"
            : "Exclusion Mode (" + totalExcluded + " block(s) excluded)";
        
        info.addEntry(entryBuilder.startTextDescription(Text.literal("Current Mode: " + modeText))
                .setTooltip(Text.literal("Shows the current compression mode and block counts"))
                .build());
        
        // Show full exclusion list - one per line for readability
        if (!config.useAllowlist) {
            java.util.List<String> allExcluded = config.getAllActiveExclusions();
            if (!allExcluded.isEmpty()) {
                // Format as one block per line for better readability
                StringBuilder excludedList = new StringBuilder();
                excludedList.append("All Excluded Blocks (").append(allExcluded.size()).append(" total):\n\n");
                for (int i = 0; i < allExcluded.size(); i++) {
                    excludedList.append("  • ").append(allExcluded.get(i));
                    if (i < allExcluded.size() - 1) {
                        excludedList.append("\n");
                    }
                }
                
                info.addEntry(entryBuilder.startTextDescription(Text.literal(excludedList.toString()))
                        .setTooltip(Text.literal("Complete list of all blocks that cannot be compressed"))
                        .build());
            }
            
            if (config.useDefaultExclusions && defaultExcluded > 0) {
                java.util.List<String> defaultExclusions = CompressyConfig.getDefaultExclusions();
                StringBuilder defaultList = new StringBuilder();
                defaultList.append("Default Exclusions (").append(defaultExcluded).append(" blocks):\n\n");
                for (int i = 0; i < defaultExclusions.size(); i++) {
                    defaultList.append("  • ").append(defaultExclusions.get(i));
                    if (i < defaultExclusions.size() - 1) {
                        defaultList.append("\n");
                    }
                }
                
                info.addEntry(entryBuilder.startTextDescription(Text.literal(defaultList.toString()))
                        .setTooltip(Text.literal("Technical and non-solid blocks automatically excluded from compression"))
                        .build());
            }
            
            if (userExcluded > 0) {
                StringBuilder customList = new StringBuilder();
                customList.append("Custom Exclusions (").append(userExcluded).append(" block(s)):\n\n");
                for (int i = 0; i < config.excludedBlocks.size(); i++) {
                    customList.append("  • ").append(config.excludedBlocks.get(i));
                    if (i < config.excludedBlocks.size() - 1) {
                        customList.append("\n");
                    }
                }
                
                info.addEntry(entryBuilder.startTextDescription(Text.literal(customList.toString()))
                        .setTooltip(Text.literal("Additional blocks you've manually excluded"))
                        .build());
            }
        }
        
        // Show full allowlist - one per line for readability
        if (config.useAllowlist && allowed > 0) {
            StringBuilder allowedList = new StringBuilder();
            allowedList.append("Allowed Blocks (").append(allowed).append(" total):\n\n");
            for (int i = 0; i < config.allowedBlocks.size(); i++) {
                allowedList.append("  • ").append(config.allowedBlocks.get(i));
                if (i < config.allowedBlocks.size() - 1) {
                    allowedList.append("\n");
                }
            }
            
            info.addEntry(entryBuilder.startTextDescription(Text.literal(allowedList.toString()))
                    .setTooltip(Text.literal("Complete list of blocks that can be compressed in allowlist mode"))
                    .build());
        }
        
        // === DISPLAY CATEGORY ===
        // Only show display settings in FULL mode (LITE mode doesn't place blocks)
        if (!CompressyMod.LITE_MODE) {
            ConfigCategory display = builder.getOrCreateCategory(Text.literal("Display"));
            
            display.addEntry(entryBuilder.startBooleanToggle(
                    Text.literal("Show Roman Numerals"),
                    config.showRomanNumerals)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Show tier labels (I, II, III...) above placed compressed blocks.\n\nOnly available in FULL mode."))
                    .setSaveConsumer(val -> config.showRomanNumerals = val)
                    .build());
            
            display.addEntry(entryBuilder.startBooleanToggle(
                    Text.literal("Show Darkening Overlay"),
                    config.showDarkeningOverlay)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Show darkening effect on higher tier compressed blocks.\n\nHigher compression levels get progressively darker overlays for visual feedback.\n\nOnly available in FULL mode."))
                    .setSaveConsumer(val -> config.showDarkeningOverlay = val)
                    .build());
        }
        
        // === BLOCK MANAGEMENT CATEGORY ===
        ConfigCategory blocks = builder.getOrCreateCategory(Text.literal("Block Management"));
        
        // Allowlist mode toggle
        blocks.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Use Allowlist Mode"),
                config.useAllowlist)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Allowlist Mode: Only blocks in the allowlist can be compressed.\n\nExclusion Mode: All blocks can be compressed EXCEPT those in the exclusion list.\n\nDefault: Exclusion Mode (recommended for most users)"))
                .setSaveConsumer(val -> config.useAllowlist = val)
                .build());
        
        // Allowlist block list
        blocks.addEntry(entryBuilder.startStrList(
                Text.literal("Allowed Blocks"),
                config.allowedBlocks)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that CAN be compressed (when allowlist mode is enabled).\n\nTo add: Click the + button, then type a block ID in the input field.\nTo edit: Click on any entry to modify it.\nTo remove: Click the X button on an entry.\n\nFormat: namespace:blockname (e.g., minecraft:stone)"))
                .setInsertButtonEnabled(true)
                .setDeleteButtonEnabled(true)
                .setAddButtonTooltip(Text.literal("Click to add a new block ID.\nA text input will appear - type the block ID and press Enter."))
                .setRemoveButtonTooltip(Text.literal("Click to remove this block from the allowlist"))
                .setSaveConsumer(val -> {
                    config.allowedBlocks.clear();
                    config.allowedBlocks.addAll(val);
                })
                .build());
        
        // Default exclusions toggle (only shown when NOT in allowlist mode)
        blocks.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Use Default Exclusions"),
                config.useDefaultExclusions)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Automatically exclude technical and non-solid blocks.\n\nIncludes: air, torches, rails, beds, doors, command blocks, etc.\n\nRecommended: Enabled (prevents compressing blocks that don't make sense)\n\nOnly applies when Allowlist Mode is disabled."))
                .setSaveConsumer(val -> config.useDefaultExclusions = val)
                .build());
        
        // Exclusion block list
        blocks.addEntry(entryBuilder.startStrList(
                Text.literal("Excluded Blocks"),
                config.excludedBlocks)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that CANNOT be compressed (when allowlist mode is disabled).\n\nTo add: Click the + button, then type a block ID in the input field.\nTo edit: Click on any entry to modify it.\nTo remove: Click the X button on an entry.\n\nFormat: namespace:blockname (e.g., minecraft:bedrock)"))
                .setInsertButtonEnabled(true)
                .setDeleteButtonEnabled(true)
                .setAddButtonTooltip(Text.literal("Click to add a new block ID.\nA text input will appear - type the block ID and press Enter."))
                .setRemoveButtonTooltip(Text.literal("Click to remove this block from the exclusion list"))
                .setSaveConsumer(val -> {
                    config.excludedBlocks.clear();
                    config.excludedBlocks.addAll(val);
                })
                .build());
        
        return builder.build();
    }
}

