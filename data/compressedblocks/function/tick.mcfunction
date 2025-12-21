# ========================================
# Compressed Blocks - Tick Function
# ========================================
# Runs every tick - OPTIMIZED for minimal performance impact
# Only processes when necessary using scoreboards and tags

# Process compressor interactions (only when players interact)
execute as @a[scores={cb.use_carrot=1..}] at @s run function compressedblocks:compressor/check_interaction
execute as @a run scoreboard players set @s cb.use_carrot 0

# Process placed compressed blocks (visual updates)
# Only runs if there are any compressed block displays
execute if entity @e[type=block_display,tag=cb.compressed_display,limit=1] run function compressedblocks:display/update_all

# Reduce cooldowns
execute as @a[scores={cb.cooldown=1..}] run scoreboard players remove @s cb.cooldown 1

