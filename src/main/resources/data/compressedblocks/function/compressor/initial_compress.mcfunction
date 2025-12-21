# ========================================
# Initial Compression (Level 0 -> Level 1)
# ========================================
# Compresses regular blocks into level 1 compressed blocks

# Store the block ID for the compressed block
data modify storage compressedblocks:temp block_id set from entity @s SelectedItem.id

# Check if it's a compressible block (has block form)
# We use the item ID directly as the block reference

# Get current count
execute store result score #count cb.temp run data get entity @s SelectedItem.count

# Calculate how many level-1 compressions we can make (count / 9)
scoreboard players operation #compress_count cb.temp = #count cb.temp
scoreboard players operation #compress_count cb.temp /= #9 cb.data

# Calculate remainder
scoreboard players operation #remainder cb.temp = #count cb.temp
scoreboard players operation #used cb.temp = #compress_count cb.temp
scoreboard players operation #used cb.temp *= #9 cb.data
scoreboard players operation #remainder cb.temp -= #used cb.temp

# Can't compress if we don't have enough
execute unless score #compress_count cb.temp matches 1.. run return run tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Need at least 9 blocks to compress!","color":"red"}]

# Remove the used blocks from inventory
execute store result storage compressedblocks:temp clear_count int 1 run scoreboard players get #used cb.temp

# Clear the blocks that will be compressed
# We need to use item modifier or loot table approach
# For now, use a clear command with the count

# Store item id for clear command
data modify storage compressedblocks:temp item_to_clear set from entity @s SelectedItem.id

# Execute the item giving with macro
function compressedblocks:compressor/give_compressed with storage compressedblocks:temp

