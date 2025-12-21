package com.compressy.util;

import net.minecraft.nbt.NbtCompound;

/**
 * NBT helper for Minecraft 1.21.11+
 * Uses the Optional-returning NBT API.
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
            return nbt.getBoolean(key).orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
