# ========================================
# Update All Compressed Block Displays
# ========================================
# Updates visual indicators on placed Compressy
# Only runs when compressed block displays exist

# Update text displays to pulse/glow effect
execute as @e[type=text_display,tag=cb.level_display] at @s run function compressedblocks:display/update_text

