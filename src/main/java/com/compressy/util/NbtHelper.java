package com.compressy.util;

import com.compressy.CompressyMod;
import net.minecraft.nbt.NbtCompound;

/**
 * NBT helper for Minecraft 1.21.11+
 * Uses the Optional-returning NBT API.
 */
public class NbtHelper {
    
    /**
     * Get an int from NBT, with fallback for missing keys.
     * More robust with multiple fallback methods.
     */
    public static int getInt(NbtCompound nbt, String key, int defaultValue) {
        if (nbt == null) {
            CompressyMod.LOGGER.debug("NbtHelper.getInt: nbt is null for key {}", key);
            return defaultValue;
        }
        
        if (!nbt.contains(key)) {
            CompressyMod.LOGGER.debug("NbtHelper.getInt: key '{}' not found in NBT. Available keys: {}", key, nbt.getKeys());
            return defaultValue;
        }
        
        try {
            var result = nbt.getInt(key);
            if (result.isPresent()) {
                int value = result.get();
                CompressyMod.LOGGER.debug("NbtHelper.getInt: key '{}' = {}", key, value);
                return value;
            } else {
                CompressyMod.LOGGER.warn("NbtHelper.getInt: key '{}' exists but Optional is empty", key);
                return defaultValue;
            }
        } catch (Exception e) {
            CompressyMod.LOGGER.error("NbtHelper.getInt: Exception reading key '{}' from NBT", key, e);
            return defaultValue;
        }
    }
    
    /**
     * Get a string from NBT, with fallback for missing keys.
     */
    public static String getString(NbtCompound nbt, String key, String defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        try {
            var result = nbt.getString(key);
            return result.orElse(defaultValue);
        } catch (Exception e) {
            CompressyMod.LOGGER.error("NbtHelper.getString: Exception reading key '{}' from NBT", key, e);
            return defaultValue;
        }
    }
    
    /**
     * Get a boolean from NBT, with fallback for missing keys.
     */
    public static boolean getBoolean(NbtCompound nbt, String key, boolean defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        try {
            var result = nbt.getBoolean(key);
            return result.orElse(defaultValue);
        } catch (Exception e) {
            CompressyMod.LOGGER.error("NbtHelper.getBoolean: Exception reading key '{}' from NBT", key, e);
            return defaultValue;
        }
    }
}
