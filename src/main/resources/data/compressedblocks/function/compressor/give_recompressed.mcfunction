# ========================================
# Give Re-Compressed Block (Macro Function)
# ========================================
# Gives higher-level compressed block
# Arguments: block_id, new_level, block_count_str

# Clear 9 of the current compressed blocks (we're combining them)
clear @s *[custom_data~{compressed_level:1}] 9

# Give the new higher-level compressed block
$give @s $(block_id)[custom_name='{"text":"Compressed $(block_id) [Lv.$(new_level)]","color":"light_purple","italic":false,"bold":true}',lore=['{"text":"Compression Level: $(new_level)","color":"yellow","italic":false}','{"text":"Contains: $(block_count_str) blocks","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Right-click compressor to compress more!","color":"green","italic":false}'],custom_data={compressed_level:$(new_level),compressed_block:"$(block_id)"},enchantment_glint_override=true] 1

# Play success sound with pitch based on level
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 1 2
particle minecraft:end_rod ~ ~1 ~ 0.5 0.5 0.5 0.1 20

$tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Compressed to Level $(new_level)!","color":"light_purple","bold":true}]

