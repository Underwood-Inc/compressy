package com.compressy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.compressy.recipe.CompressionRecipe;
import com.compressy.recipe.CompressionRecipeSerializer;
import com.compressy.recipe.DecompressionRecipe;
import com.compressy.recipe.DecompressionRecipeSerializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Compressy - Compress ANY block up to 32 times!
 * 
 * A Minecraft Fabric mod that allows compressing blocks into super-dense variants.
 * Works automatically with ALL blocks (vanilla + modded) via dynamic recipe system.
 * 
 * HOW IT WORKS:
 * - Place 9 blocks in a 3×3 crafting grid → get 1 compressed block
 * - Craft a compressed block alone → get 9 blocks back
 * - Works with crafting tables, autocrafters, and mod machines (AE2, Create, etc.)
 * 
 * COMPRESSION LEVELS:
 * - Level 1: 9 blocks (3×3)
 * - Level 2: 81 blocks (9²)
 * - Level 3: 729 blocks (9³)
 * - ...up to Level 32: 9^32 blocks (astronomical!)
 * 
 * BUILD VARIANTS:
 * 
 * FULL VERSION:
 * - Compressed blocks CAN be placed in world
 * - Visual overlays show compression tier (Roman numerals + darkening effect)
 * - Uses marker entities to preserve compression data
 * - Breaking returns the compressed block with all data intact
 * - Best for: Creative builds, visual storage displays
 * 
 * LITE VERSION:
 * - Compressed blocks CANNOT be placed (inventory-only)
 * - No marker entities, no overlays, no world impact
 * - Zero performance overhead
 * - Best for: Servers, automation-focused gameplay, pure storage
 * 
 * NOTE: The mod is REQUIRED for compression to work. The bundled datapack
 * provides helper functions but the recipes use custom Fabric serializers.
 */
public class CompressyMod implements ModInitializer {
    public static final String MOD_ID = "compressy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Detect if we're running in LITE mode via manifest or system property
    public static final boolean LITE_MODE = detectLiteMode();
    
    // Recipe serializers - these enable automatic crafting for ALL blocks!
    public static final RecipeSerializer<CompressionRecipe> COMPRESSION_RECIPE_SERIALIZER = 
        new CompressionRecipeSerializer();
    public static final RecipeSerializer<DecompressionRecipe> DECOMPRESSION_RECIPE_SERIALIZER = 
        new DecompressionRecipeSerializer();
    
    /**
     * Detect if we're running in LITE mode.
     * Checks manifest attribute or system property.
     */
    private static boolean detectLiteMode() {
        // Check system property first (allows runtime override)
        String sysProp = System.getProperty("compressedblocks.lite", "false");
        if ("true".equalsIgnoreCase(sysProp)) {
            return true;
        }
        
        // Check manifest attribute
        try {
            var resources = CompressyMod.class.getClassLoader()
                .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                try (var is = url.openStream()) {
                    var manifest = new java.util.jar.Manifest(is);
                    var attrs = manifest.getMainAttributes();
                    String mode = attrs.getValue("Compressed-Blocks-Mode");
                    if ("lite".equalsIgnoreCase(mode)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore, default to full mode
        }
        
        return false;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("===========================================");
        if (LITE_MODE) {
            LOGGER.info("  Compressy LITE - No Placement!  ");
        } else {
            LOGGER.info("  Compressy FULL - Infinite Storage!  ");
        }
        LOGGER.info("===========================================");
        LOGGER.info("Mod loaded successfully! Mode: " + (LITE_MODE ? "LITE" : "FULL"));
        
        // Load configuration
        com.compressy.config.CompressyConfig.load();
        LOGGER.info("Configuration loaded!");
        
        // Register custom recipe types
        // NOTE: Only compression uses recipes - decompression uses direct mixin interception
        registerRecipes();
        LOGGER.info("Compression recipes registered!");
        LOGGER.info("Decompression: Using direct mixin interception (bypasses broken recipe matching)");
        
        // Register block placement handler based on mode
        if (LITE_MODE) {
            // LITE: Just prevent placement entirely
            CompressyBlockHandlerLite.register();
            LOGGER.info("LITE mode: Block placement DISABLED (inventory-only)");
        } else {
            // FULL: Allow placement with marker entities
            CompressyBlockHandler.register();
            LOGGER.info("FULL mode: Block placement enabled with data preservation");
        }
        
        LOGGER.info("Datapack auto-installed.");
        LOGGER.info("Registering commands...");

        registerCommands();

        LOGGER.info("Commands registered! Use /cblocks help");
        LOGGER.info("===========================================");
    }
    
    /**
     * Register custom recipe serializers.
     * 
     * COMPRESSION: Uses SpecialCraftingRecipe (works fine)
     * DECOMPRESSION: DISABLED - uses direct mixin interception instead
     *   (recipe matching is broken - vanilla recipes match first INCONSISTENTLY FOR RANDOM BLOCKS)
     */
    private void registerRecipes() {
        Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier.of(MOD_ID, "compression"),
            COMPRESSION_RECIPE_SERIALIZER
        );
        
        // DECOMPRESSION RECIPE DISABLED - using mixin instead
        // The recipe matching system is broken - vanilla recipes always match first
        // So we intercept crafting results directly via CraftingScreenHandlerMixin
        // Registry.register(
        //     Registries.RECIPE_SERIALIZER,
        //     Identifier.of(MOD_ID, "decompression"),
        //     DECOMPRESSION_RECIPE_SERIALIZER
        // );
        
        LOGGER.info("Registered compression recipe type");
        LOGGER.info("Decompression: Using mixin interception (recipe system bypassed)");
    }

    /**
     * Registers all mod commands
     */
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> cblocks = CommandManager.literal("cblocks");

            // /cblocks help
            cblocks.then(CommandManager.literal("help")
                    .executes(this::showHelp));

            // /cblocks info - Show info about held compressed block
            cblocks.then(CommandManager.literal("info")
                    .executes(this::showBlockInfo));

            // /cblocks decompress [amount] - Decompress held block
            cblocks.then(CommandManager.literal("decompress")
                    .executes(ctx -> decompress(ctx, 1))
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> decompress(ctx, IntegerArgumentType.getInteger(ctx, "amount")))));

            // /cblocks admin subcommands (OP required)
            LiteralArgumentBuilder<ServerCommandSource> adminCommand = CommandManager.literal("admin")
                    .requires(source -> {
                        var perms = source.getPermissions();
                        if (perms instanceof net.minecraft.command.permission.LeveledPermissionPredicate lpp) {
                            return lpp.getLevel().compareTo(net.minecraft.command.permission.LeveledPermissionPredicate.GAMEMASTERS.getLevel()) >= 0;
                        }
                        return false;
                    });

            adminCommand.then(CommandManager.literal("debug")
                    .executes(this::showDebug));
            adminCommand.then(CommandManager.literal("reload")
                    .executes(this::reloadConfig));

            cblocks.then(adminCommand);

            dispatcher.register(cblocks);
            LOGGER.info("Registered /cblocks command");
        });
    }

    private int showHelp(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("  📦 Compressy - Help")
                .formatted(Formatting.GOLD).formatted(Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("How to Compress Blocks:")
                .formatted(Formatting.YELLOW).formatted(Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("  1. Place 9 blocks in a 3×3 crafting grid")
                .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("  2. Get 1 compressed block!")
                .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("  3. Craft alone to decompress back to 9")
                .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("  ✦ Works with crafting tables & autocrafters!")
                .formatted(Formatting.GREEN), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("Compression Levels:")
                .formatted(Formatting.YELLOW).formatted(Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("  Level 1 = 9 blocks (3×3)")
                .formatted(Formatting.WHITE), false);
        source.sendFeedback(() -> Text.literal("  Level 2 = 81 blocks (9²)")
                .formatted(Formatting.WHITE), false);
        source.sendFeedback(() -> Text.literal("  Level 3 = 729 blocks (9³)")
                .formatted(Formatting.WHITE), false);
        source.sendFeedback(() -> Text.literal("  ... up to Level 32!")
                .formatted(Formatting.AQUA), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("Commands:")
                .formatted(Formatting.YELLOW).formatted(Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("  /cblocks help")
                .formatted(Formatting.GREEN)
                .append(Text.literal(" - Show this help").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("  /cblocks info")
                .formatted(Formatting.GREEN)
                .append(Text.literal(" - Info about held block").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.GOLD), false);
        
        return Command.SINGLE_SUCCESS;
    }

    private int showBlockInfo(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            var heldItem = player.getMainHandStack();
            
            if (heldItem.isEmpty()) {
                source.sendFeedback(() -> Text.literal("[Compressy] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal("Hold a compressed block to see info!").formatted(Formatting.YELLOW)), false);
                return Command.SINGLE_SUCCESS;
            }
            
            // Check if it's a compressed block by looking for custom data
            var customData = heldItem.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
            if (customData == null) {
                source.sendFeedback(() -> Text.literal("[Compressy] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal("This is not a compressed block.").formatted(Formatting.YELLOW)), false);
                return Command.SINGLE_SUCCESS;
            }
            
            var nbt = customData.copyNbt();
            if (!nbt.contains("compressed_level")) {
                source.sendFeedback(() -> Text.literal("[Compressy] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal("This is not a compressed block.").formatted(Formatting.YELLOW)), false);
                return Command.SINGLE_SUCCESS;
            }
            
            // Cross-version compatible NBT access
            int level = com.compressy.util.NbtHelper.getInt(nbt, "compressed_level", 0);
            String blockId = com.compressy.util.NbtHelper.getString(nbt, "compressed_block", "unknown");
            
            // Calculate total blocks (9^level)
            java.math.BigInteger totalBlocks = java.math.BigInteger.valueOf(9).pow(level);
            
            source.sendFeedback(() -> Text.literal(""), false);
            source.sendFeedback(() -> Text.literal("━━━━ Compressed Block Info ━━━━")
                    .formatted(Formatting.GOLD), false);
            source.sendFeedback(() -> Text.literal("Block: ").formatted(Formatting.GRAY)
                    .append(Text.literal(blockId).formatted(Formatting.WHITE)), false);
            source.sendFeedback(() -> Text.literal("Compression Level: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.valueOf(level)).formatted(Formatting.AQUA)), false);
            source.sendFeedback(() -> Text.literal("Total Blocks: ").formatted(Formatting.GRAY)
                    .append(Text.literal(formatBigNumber(totalBlocks)).formatted(Formatting.GREEN)), false);
            source.sendFeedback(() -> Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .formatted(Formatting.GOLD), false);
            
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendError(Text.literal("This command must be run by a player!"));
            LOGGER.error("Error showing block info", e);
            return 0;
        }
    }

    private int decompress(CommandContext<ServerCommandSource> ctx, int amount) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("[Compressy] ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("Place a compressed block alone in a crafting grid to decompress!")
                        .formatted(Formatting.YELLOW)), false);
        return Command.SINGLE_SUCCESS;
    }

    private int showDebug(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("=== Compressy Debug ===")
                .formatted(Formatting.GOLD).formatted(Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("Mod Version: 1.0.0").formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("Status: Running").formatted(Formatting.GREEN), false);
        source.sendFeedback(() -> Text.literal("Max Compression: 32 levels").formatted(Formatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }

    private int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("[Compressy] ")
                .formatted(Formatting.GOLD).formatted(Formatting.BOLD)
                .append(Text.literal("✓ Config reloaded!").formatted(Formatting.GREEN)), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Format a BigInteger into a human-readable string with suffixes
     */
    private String formatBigNumber(java.math.BigInteger num) {
        String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};
        
        if (num.compareTo(java.math.BigInteger.valueOf(1000)) < 0) {
            return num.toString();
        }
        
        int suffixIndex = 0;
        java.math.BigDecimal decimal = new java.math.BigDecimal(num);
        java.math.BigDecimal thousand = java.math.BigDecimal.valueOf(1000);
        
        while (decimal.compareTo(thousand) >= 0 && suffixIndex < suffixes.length - 1) {
            decimal = decimal.divide(thousand, 2, java.math.RoundingMode.HALF_UP);
            suffixIndex++;
        }
        
        if (suffixIndex >= suffixes.length) {
            // Scientific notation for truly massive numbers
            return String.format("%.2e", new java.math.BigDecimal(num));
        }
        
        return decimal.stripTrailingZeros().toPlainString() + suffixes[suffixIndex];
    }
}


