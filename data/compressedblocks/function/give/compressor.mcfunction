# ========================================
# Give Compressor Block
# ========================================
# Gives the player a Block Compressor (lodestone with custom data)

give @s lodestone[custom_name='{"text":"Block Compressor","color":"gold","italic":false,"bold":true}',lore=['{"text":"Right-click with blocks to compress!","color":"gray","italic":false}','{"text":"Shift+right-click to decompress","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Supports up to 32x compression!","color":"aqua","italic":false}'],custom_data={compressedblocks_compressor:1b},enchantment_glint_override=true] 1

tellraw @s [{"text":"[Compressed Blocks] ","color":"gold","bold":true},{"text":"Use the Compressor block!","color":"green"}]
tellraw @s [{"text":"  • Place the block","color":"gray"}]
tellraw @s [{"text":"  • Right-click with blocks to compress","color":"gray"}]
tellraw @s [{"text":"  • Shift+right-click to decompress","color":"gray"}]

