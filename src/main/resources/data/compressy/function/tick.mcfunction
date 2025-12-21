# ========================================
# Compressy - Tick Function
# ========================================
# Runs every tick - OPTIMIZED for minimal performance impact
# Only processes when necessary using scoreboards and tags

# Process placed Compressy (visual updates)
# Only runs if there are any compressed block displays
execute if entity @e[type=block_display,tag=cb.compressed_display,limit=1] run function compressy:display/update_all

# Reduce cooldowns
execute as @a[scores={cb.cooldown=1..}] run scoreboard players remove @s cb.cooldown 1

