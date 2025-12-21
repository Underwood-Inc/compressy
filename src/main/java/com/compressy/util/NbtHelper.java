package com.compressy.util;

import net.minecraft.nbt.NbtCompound;

/**
 * NBT helper for cross-version compatibility.
 * 
 * Handles the 1.21.11+ Optional-returning NBT API.
 * For older versions, this will need adjustments in a multi-version setup.
 */
public class NbtHelper {
    
    /**
     * Get an int from NBT, with fallback for missing keys.
     */
    public static int getInt(NbtCompound nbt, String key, int defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        try {
            // 1.21.11 API - getInt returns Optional<Integer>
            return nbt.getInt(key).orElse(defaultValue);
        } catch (Exception e) {
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
            // 1.21.11 API - getString returns Optional<String>
            return nbt.getString(key).orElse(defaultValue);
        } catch (Exception e) {
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
            // 1.21.11 API - getBoolean returns Optional<Boolean>
            return nbt.getBoolean(key).orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
