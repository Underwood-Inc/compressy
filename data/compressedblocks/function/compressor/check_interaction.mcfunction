# ========================================
# Check Compressor Interaction
# ========================================
# Called when player uses carrot_on_a_stick (our interaction detector)
# Checks if player is near a compressor block and holding compressible items

# Reset use counter
scoreboard players set @s cb.use_carrot 0

# Check if player is on cooldown
execute if score @s cb.cooldown matches 1.. run return 0

# Check if player is looking at a lodestone with compressor tag
# We use the block the player is looking at
execute unless block ~ ~ ~ lodestone run execute unless block ~ ~-1 ~ lodestone run execute unless block ~ ~1 ~ lodestone run return 0

# Store the block being held for compression logic
execute store result score @s cb.temp run clear @s #compressedblocks:compressible 0

# If player has compressible items, try to compress
execute if score @s cb.temp matches 1.. run function compressedblocks:compressor/compress

# Set cooldown to prevent spam (5 ticks = 0.25 seconds)
scoreboard players set @s cb.cooldown 5

