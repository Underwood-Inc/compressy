# ========================================
# Process Held Item for Compression
# ========================================
# Determines what the player is holding and processes compression

# Get the selected slot
execute store result score #slot cb.temp run data get entity @s SelectedItemSlot

# Store hand item info
execute store result score #count cb.temp run data get entity @s SelectedItem.count

# Check if we have at least 9 items
execute unless score #count cb.temp matches 9.. run return run tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Need at least 9 blocks to compress!","color":"red"}]

# Check if the item already has compression data (re-compressing)
execute if data entity @s SelectedItem.components."minecraft:custom_data".compressed_level run function compressedblocks:compressor/recompress
execute unless data entity @s SelectedItem.components."minecraft:custom_data".compressed_level run function compressedblocks:compressor/initial_compress

