# ========================================
# Execute Initial Compression (Macro)
# ========================================
# Arguments: block_id, used_count, give_count

# Remove blocks from offhand
$item modify entity @s weapon.offhand compressedblocks:remove_$(used_count)

# Give compressed blocks
$give @s $(block_id)[custom_name='{"text":"Compressed Block [Lv.1]","color":"aqua","italic":false,"bold":true}',lore=['{"text":"Compression Level: 1","color":"yellow","italic":false}','{"text":"Contains: 9 blocks each","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Use wand again to compress more!","color":"green","italic":false}'],custom_data={compressed_level:1,compressed_block:"$(block_id)",block_count:9},enchantment_glint_override=true] $(give_count)

# Effects
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 1 1.5
particle minecraft:happy_villager ~ ~1 ~ 0.5 0.5 0.5 0 10

$tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Compressed $(give_count)x to Level 1!","color":"green"}]

