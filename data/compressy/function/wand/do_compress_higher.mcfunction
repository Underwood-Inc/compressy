# ========================================
# Execute Higher Compression (Macro)
# ========================================
# Arguments: block_id, new_level, block_count_str, give_count

# Remove 9 items per compression from offhand (simplified - removes all for now)
item modify entity @s weapon.offhand compressy:decrement_9

# Give higher level Compressy
$give @s $(block_id)[custom_name='{"text":"Compressed Block [Lv.$(new_level)]","color":"light_purple","italic":false,"bold":true}',lore=['{"text":"Compression Level: $(new_level)","color":"yellow","italic":false}','{"text":"Contains: $(block_count_str) blocks","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Use wand again to compress more!","color":"green","italic":false}'],custom_data={compressed_level:$(new_level),compressed_block:"$(block_id)"},enchantment_glint_override=true] $(give_count)

# Effects based on level
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 1 2
particle minecraft:end_rod ~ ~1 ~ 0.5 0.5 0.5 0.1 20

$tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Compressed to Level $(new_level)!","color":"light_purple","bold":true}]

