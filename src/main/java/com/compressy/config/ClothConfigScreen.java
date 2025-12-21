package com.compressy.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

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
        
        // === EXCLUSIONS CATEGORY ===
        ConfigCategory exclusions = builder.getOrCreateCategory(Text.literal("Block Exclusions"));
        
        exclusions.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Use Default Exclusions"),
                config.useDefaultExclusions)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Exclude technical/non-solid blocks by default"))
                .setSaveConsumer(val -> config.useDefaultExclusions = val)
                .build());
        
        exclusions.addEntry(entryBuilder.startStrList(
                Text.literal("Custom Excluded Blocks"),
                config.excludedBlocks)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Block IDs to exclude (e.g., minecraft:diamond_block)"))
                .setSaveConsumer(val -> {
                    config.excludedBlocks.clear();
                    config.excludedBlocks.addAll(val);
                })
                .build());
        
        return builder.build();
    }
}

