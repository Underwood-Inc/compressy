# ========================================
# Compression Wand Use
# ========================================
# Called when player uses the compression wand
# Checks offhand for blocks to compress

# Revoke the advancement so it can trigger again
advancement revoke @s only compressedblocks:technical/detect_compress

# Check if player is sneaking (decompress mode)
execute if entity @s[nbt={OnGround:1b}] if predicate compressedblocks:player/is_sneaking run function compressedblocks:wand/decompress

# Normal mode - compress blocks in offhand
execute unless predicate compressedblocks:player/is_sneaking run function compressedblocks:wand/compress

