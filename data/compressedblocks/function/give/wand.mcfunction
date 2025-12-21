# ========================================
# Give Compression Wand
# ========================================
# Gives the player a Compression Wand for compressing blocks

give @s carrot_on_a_stick[custom_name='{"text":"Compression Wand","color":"gold","italic":false,"bold":true}',lore=['{"text":"Right-click to compress held blocks!","color":"gray","italic":false}','{"text":"Shift+right-click to decompress","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Uses 9 blocks per compression","color":"aqua","italic":false}'],custom_data={compressedblocks_wand:1b},enchantment_glint_override=true] 1

tellraw @s [{"text":"[Compressed Blocks] ","color":"gold","bold":true},{"text":"Compression Wand received!","color":"green"}]
tellraw @s [{"text":"  • Hold blocks in offhand","color":"gray"}]
tellraw @s [{"text":"  • Right-click to compress","color":"gray"}]
tellraw @s [{"text":"  • Shift+right-click to decompress","color":"gray"}]

