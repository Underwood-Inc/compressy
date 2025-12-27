package com.compressy.config;

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
        CompressyConfig config = CompressyConfig.get();
        
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Compressy Configuration"));
        
        builder.setSavingRunnable(CompressyConfig::save);
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        // === DISPLAY CATEGORY ===
        ConfigCategory display = builder.getOrCreateCategory(Text.literal("Display"));
        
        display.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Show Roman Numerals"),
                config.showRomanNumerals)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show tier labels (I, II, III...) above placed compressed blocks"))
                .setSaveConsumer(val -> config.showRomanNumerals = val)
                .build());
        
        display.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Show Darkening Overlay"),
                config.showDarkeningOverlay)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show darkening effect on higher tier compressed blocks"))
                .setSaveConsumer(val -> config.showDarkeningOverlay = val)
                .build());
        
        // === BLOCK MANAGEMENT CATEGORY ===
        ConfigCategory blocks = builder.getOrCreateCategory(Text.literal("Block Management"));
        
        // Allowlist mode toggle
        blocks.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Use Allowlist Mode"),
                config.useAllowlist)
                .setDefaultValue(false)
                .setTooltip(Text.literal("If enabled, ONLY blocks in the allowlist can be compressed.\nIf disabled, all blocks can be compressed EXCEPT those in the exclusion list."))
                .setSaveConsumer(val -> config.useAllowlist = val)
                .build());
        
        // Allowlist block picker - using string list with block picker button
        // Note: Cloth Config's block picker opens a searchable block selection screen
        blocks.addEntry(entryBuilder.startStrList(
                Text.literal("Allowed Blocks"),
                config.allowedBlocks)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that CAN be compressed (when allowlist mode is enabled).\nClick the + button to add blocks by ID (e.g., minecraft:stone).\nYou can also use the block picker button to visually select blocks."))
                .setInsertButtonEnabled(true)
                .setAddButtonTooltip(Text.literal("Add a block ID (e.g., minecraft:dirt)"))
                .setRemoveButtonTooltip(Text.literal("Remove this block from the allowlist"))
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
                .setTooltip(Text.literal("Exclude technical/non-solid blocks by default (only applies when allowlist mode is disabled)"))
                .setSaveConsumer(val -> config.useDefaultExclusions = val)
                .build());
        
        // Exclusion block picker - using string list with block picker button
        blocks.addEntry(entryBuilder.startStrList(
                Text.literal("Excluded Blocks"),
                config.excludedBlocks)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that CANNOT be compressed (when allowlist mode is disabled).\nClick the + button to add blocks by ID (e.g., minecraft:torch).\nYou can also use the block picker button to visually select blocks."))
                .setInsertButtonEnabled(true)
                .setAddButtonTooltip(Text.literal("Add a block ID (e.g., minecraft:bedrock)"))
                .setRemoveButtonTooltip(Text.literal("Remove this block from the exclusion list"))
                .setSaveConsumer(val -> {
                    config.excludedBlocks.clear();
                    config.excludedBlocks.addAll(val);
                })
                .build());
        
        return builder.build();
    }
}

