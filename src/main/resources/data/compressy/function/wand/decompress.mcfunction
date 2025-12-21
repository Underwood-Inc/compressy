# ========================================
# Wand Decompress
# ========================================
# Decompresses blocks in offhand

# Check if offhand has Compressy
execute unless data entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_level run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Put Compressy in offhand to decompress!","color":"yellow"}]

# Get current level
execute store result score #level cb.temp run data get entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_level

# Get block ID
data modify storage compressy:temp block_id set from entity @s Inventory[{Slot:-106b}].components."minecraft:custom_data".compressed_block

# If level 1, give base blocks
execute if score #level cb.temp matches 1 run function compressy:wand/decompress_to_base with storage compressy:temp

# If level > 1, give level-1 blocks
execute if score #level cb.temp matches 2.. run function compressy:wand/decompress_to_lower with storage compressy:temp

