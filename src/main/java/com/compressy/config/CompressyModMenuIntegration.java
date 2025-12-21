package com.compressy.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration for Compressy.
 * 
 * Provides a config screen when ModMenu is installed.
 * If Cloth Config is available, uses that for a fancy GUI.
 * Otherwise, shows a simple info screen.
 */
public class CompressyModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Try to use Cloth Config if available and compatible
            try {
                // Check if Cloth Config is available at all
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                return ClothConfigScreen.create(parent);
            } catch (Throwable e) {
                // Any error (NoClassDefFoundError, ClassNotFoundException, 
                // IncompatibleClassChangeError, etc.) - use simple screen
                return new SimpleConfigScreen(parent);
            }
        };
    }
}

