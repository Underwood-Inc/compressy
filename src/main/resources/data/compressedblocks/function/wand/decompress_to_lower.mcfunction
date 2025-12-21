# ========================================
# Decompress to Lower Level (Macro)
# ========================================
# Arguments: block_id

# Calculate new level
scoreboard players remove #level cb.temp 1
execute store result storage compressedblocks:temp new_level int 1 run scoreboard players get #level cb.temp

# Calculate block count string for new level
function compressedblocks:compressor/calculate_block_count

# Get count of Compressy
execute store result score #count cb.temp run data get entity @s Inventory[{Slot:-106b}].count

# Calculate give count (current count * 9)
scoreboard players operation #give_count cb.temp = #count cb.temp
scoreboard players operation #give_count cb.temp *= #9 cb.data

execute store result storage compressedblocks:temp give_count int 1 run scoreboard players get #give_count cb.temp

# Clear offhand
item replace entity @s weapon.offhand with air

# Give lower level blocks
$give @s $(block_id)[custom_name='{"text":"Compressed Block [Lv.$(new_level)]","color":"aqua","italic":false,"bold":true}',lore=['{"text":"Compression Level: $(new_level)","color":"yellow","italic":false}','{"text":"Contains: $(block_count_str) blocks","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Use wand again to compress more!","color":"green","italic":false}'],custom_data={compressed_level:$(new_level),compressed_block:"$(block_id)"},enchantment_glint_override=true] $(give_count)

# Effects
playsound minecraft:block.amethyst_block.break master @s ~ ~ ~ 1 0.8
particle minecraft:poof ~ ~1 ~ 0.3 0.3 0.3 0.05 10

$tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Decompressed to $(give_count)x Level $(new_level)!","color":"green"}]

