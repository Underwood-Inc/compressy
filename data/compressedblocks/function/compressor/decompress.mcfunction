# ========================================
# Decompress Block
# ========================================
# Decompresses a compressed block back to its components
# Shift+right-click on compressor with compressed block

# Check if holding a compressed block
execute unless data entity @s SelectedItem.components."minecraft:custom_data".compressed_level run return run tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Hold a compressed block to decompress!","color":"yellow"}]

# Get current level
execute store result score #level cb.temp run data get entity @s SelectedItem.components."minecraft:custom_data".compressed_level

# Get block id
data modify storage compressedblocks:temp block_id set from entity @s SelectedItem.components."minecraft:custom_data".compressed_block

# If level is 1, give back 9 regular blocks
execute if score #level cb.temp matches 1 run function compressedblocks:compressor/decompress_to_base with storage compressedblocks:temp

# If level > 1, give back 9 blocks of level-1
execute if score #level cb.temp matches 2.. run function compressedblocks:compressor/decompress_to_lower with storage compressedblocks:temp

