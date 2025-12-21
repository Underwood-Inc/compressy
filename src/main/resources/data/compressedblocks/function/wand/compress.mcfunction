# ========================================
# Wand Compress
# ========================================
# Compresses blocks in player's offhand

# Check if offhand has items
execute unless data entity @s Inventory[{Slot:-106b}] run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Put blocks in your offhand to compress!","color":"yellow"}]

# Get offhand item count
execute store result score #count cb.temp run data get entity @s Inventory[{Slot:-106b}].count

# Check if enough blocks (9+)
execute unless score #count cb.temp matches 9.. run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Need at least 9 blocks in offhand!","color":"red"}]

# Store the offhand item info
data modify storage compressedblocks:temp offhand_item set from entity @s Inventory[{Slot:-106b}]

# Check if it's already compressed
execute if data entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_level run function compressedblocks:wand/compress_higher
execute unless data entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_level run function compressedblocks:wand/compress_initial

