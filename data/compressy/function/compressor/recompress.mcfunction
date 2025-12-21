# ========================================
# Re-Compression (Level N -> Level N+1)
# ========================================
# Compresses already-Compressy to higher levels

# Get current compression level
execute store result score #current_level cb.temp run data get entity @s SelectedItem.components."minecraft:custom_data".compressed_level

# Check if already at max level (32)
execute if score #current_level cb.temp matches 32.. run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Already at maximum compression (Level 32)!","color":"red"}]

# Get current count
execute store result score #count cb.temp run data get entity @s SelectedItem.count

# Need at least 9 to compress
execute unless score #count cb.temp matches 9.. run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Need at least 9 Compressy to compress further!","color":"red"}]

# Store data for the new compressed block
data modify storage compressy:temp block_id set from entity @s SelectedItem.components."minecraft:custom_data".compressed_block
execute store result storage compressy:temp current_level int 1 run data get entity @s SelectedItem.components."minecraft:custom_data".compressed_level

# Calculate new level
scoreboard players add #current_level cb.temp 1
execute store result storage compressy:temp new_level int 1 run scoreboard players get #current_level cb.temp

# Calculate blocks per unit at new level (9^new_level)
# We store this as a string representation for extremely large numbers
function compressy:compressor/calculate_block_count

# Give the higher-level compressed block
function compressy:compressor/give_recompressed with storage compressy:temp

