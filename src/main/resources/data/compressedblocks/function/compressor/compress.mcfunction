# ========================================
# Compress Block - Main Logic
# ========================================
# Compresses blocks in player's hand into compressed variants
# Uses dynamic NBT storage to avoid 1-to-1 mapping

# Get the item in player's main hand
execute store result storage compressedblocks:temp hand_count int 1 run clear @s * 0

# Check if player has at least 9 of the held item
execute store result score @s cb.temp run data get entity @s SelectedItemSlot

# Store the held item data for processing
data modify storage compressedblocks:temp held_item set from entity @s Inventory[{Slot:0b}]

# Actually process compression based on what's being held
# This runs a series of checks to handle the compression
function compressedblocks:compressor/process_held_item

