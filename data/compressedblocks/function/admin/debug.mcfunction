# ========================================
# Admin Debug Info
# ========================================
# Shows debug information for administrators

tellraw @s [{"text":""}]
tellraw @s [{"text":"=== Compressed Blocks Debug ===","color":"gold","bold":true}]
tellraw @s [{"text":""}]
tellraw @s [{"text":"Mod Version: ","color":"gray"},{"text":"1.0.0","color":"white"}]
tellraw @s [{"text":"Max Compression: ","color":"gray"},{"text":"32 levels","color":"aqua"}]
tellraw @s [{"text":""}]

# Count compressed block displays in world
execute store result score #display_count cb.temp if entity @e[type=block_display,tag=cb.compressed_display]
tellraw @s [{"text":"Active Displays: ","color":"gray"},{"score":{"name":"#display_count","objective":"cb.temp"},"color":"yellow"}]

# Show config values
execute store result score #max_level cb.temp run data get storage compressedblocks:config max_compression_level
tellraw @s [{"text":"Config Max Level: ","color":"gray"},{"score":{"name":"#max_level","objective":"cb.temp"},"color":"white"}]

execute store result score #debug cb.temp if data storage compressedblocks:config {debug_mode:true}
execute if score #debug cb.temp matches 1 run tellraw @s [{"text":"Debug Mode: ","color":"gray"},{"text":"ENABLED","color":"green"}]
execute unless score #debug cb.temp matches 1 run tellraw @s [{"text":"Debug Mode: ","color":"gray"},{"text":"DISABLED","color":"red"}]

tellraw @s [{"text":""}]
tellraw @s [{"text":"==============================","color":"gold","bold":true}]

