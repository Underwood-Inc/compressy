# ========================================
# Admin Reset All
# ========================================
# Removes all compressed block displays and resets data

# Kill all display entities
kill @e[type=block_display,tag=cb.compressed_display]
kill @e[type=text_display,tag=cb.level_display]
kill @e[type=marker,tag=cb.marker]

# Reset scoreboards
scoreboard players reset * cb.data
scoreboard players reset * cb.level
scoreboard players reset * cb.temp

# Reinitialize
function compressedblocks:load

tellraw @a [{"text":"[Compressed Blocks] ","color":"gold","bold":true},{"text":"⚠ All compressed block displays removed!","color":"red"}]

