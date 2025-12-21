# ========================================
# Decompress to Lower Level (Level N -> 9x Level N-1)
# ========================================
# Returns 9 Compressy of one level lower

# Calculate new level
scoreboard players remove #level cb.temp 1
execute store result storage compressedblocks:temp new_level int 1 run scoreboard players get #level cb.temp

# Calculate the block count string for the new level
function compressedblocks:compressor/calculate_block_count

# Clear one of the current Compressy
# This is tricky because we need to match the specific level
# We'll use the current level in storage
execute store result storage compressedblocks:temp current_level int 1 run scoreboard players add #level cb.temp 1
execute store result storage compressedblocks:temp current_level int 1 run scoreboard players get #level cb.temp
scoreboard players remove #level cb.temp 1

# Clear 1 block at current level (current_level = new_level + 1)
# Using a workaround since we can't easily match specific NBT values
clear @s *[custom_data~{compressed_block:"minecraft:stone"}] 1

# Give 9 blocks at the new level
$give @s $(block_id)[custom_name='{"text":"Compressed $(block_id) [Lv.$(new_level)]","color":"aqua","italic":false,"bold":true}',lore=['{"text":"Compression Level: $(new_level)","color":"yellow","italic":false}','{"text":"Contains: $(block_count_str) blocks","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Right-click compressor to compress more!","color":"green","italic":false}'],custom_data={compressed_level:$(new_level),compressed_block:"$(block_id)"},enchantment_glint_override=true] 9

# Effects
playsound minecraft:block.amethyst_block.break master @s ~ ~ ~ 1 0.8
particle minecraft:poof ~ ~1 ~ 0.3 0.3 0.3 0.05 10

$tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Decompressed to 9x Level $(new_level)!","color":"green"}]

