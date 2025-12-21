# ========================================
# Higher Level Compression via Wand
# ========================================
# Compresses already-compressed blocks to next level

# Get current level
execute store result score #current_level cb.temp run data get entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_level

# Check if at max
execute if score #current_level cb.temp matches 32.. run return run tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Already at maximum compression (Level 32)!","color":"red"}]

# Get count
execute store result score #count cb.temp run data get entity @s Inventory[{Slot:-106b}].count

# Need 9 to compress
execute unless score #count cb.temp matches 9.. run return run tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Need 9 compressed blocks to compress further!","color":"red"}]

# Calculate new level
scoreboard players add #current_level cb.temp 1
execute store result storage compressedblocks:temp new_level int 1 run scoreboard players get #current_level cb.temp

# Get block ID
data modify storage compressedblocks:temp block_id set from entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_block

# Calculate block count string
function compressedblocks:compressor/calculate_block_count

# Calculate how many to make
scoreboard players operation #compress_count cb.temp = #count cb.temp
scoreboard players operation #compress_count cb.temp /= #9 cb.data
scoreboard players operation #used cb.temp = #compress_count cb.temp
scoreboard players operation #used cb.temp *= #9 cb.data

execute store result storage compressedblocks:temp give_count int 1 run scoreboard players get #compress_count cb.temp

# Execute
function compressedblocks:wand/do_compress_higher with storage compressedblocks:temp

