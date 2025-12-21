# ========================================
# Initial Compression via Wand
# ========================================
# Compresses regular blocks to level 1

# Get the item ID
data modify storage compressedblocks:temp block_id set from entity @s Inventory[{Slot:-106b}].id

# Calculate how many compressions (count / 9)
execute store result score #count cb.temp run data get entity @s Inventory[{Slot:-106b}].count
scoreboard players operation #compress_count cb.temp = #count cb.temp
scoreboard players operation #compress_count cb.temp /= #9 cb.data

# Calculate blocks to use
scoreboard players operation #used cb.temp = #compress_count cb.temp
scoreboard players operation #used cb.temp *= #9 cb.data

# Store for macro
execute store result storage compressedblocks:temp used_count int 1 run scoreboard players get #used cb.temp
execute store result storage compressedblocks:temp give_count int 1 run scoreboard players get #compress_count cb.temp

# Execute the compression
function compressedblocks:wand/do_compress_initial with storage compressedblocks:temp

