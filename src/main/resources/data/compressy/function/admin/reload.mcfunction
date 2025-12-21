# ========================================
# Admin Reload Config
# ========================================
# Reloads configuration from storage

function compressy:config/load

tellraw @s [{"text":"[Compressy] ","color":"gold","bold":true},{"text":"✓ Configuration reloaded!","color":"green"}]
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 0.5 2

