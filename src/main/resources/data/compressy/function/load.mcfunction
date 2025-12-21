# ========================================
# Compressy - Load Function
# ========================================
# This function runs once when the datapack is loaded
# Supports compression up to 32 levels (9^32 blocks!)

# Create scoreboards for tracking
scoreboard objectives add cb.data dummy
scoreboard objectives add cb.level dummy
scoreboard objectives add cb.temp dummy
scoreboard objectives add cb.cooldown dummy

# Constants
scoreboard players set #max_level cb.data 32
scoreboard players set #9 cb.data 9
scoreboard players set #1 cb.data 1
scoreboard players set #0 cb.data 0

# Load config
function compressy:config/load

# Display load message
tellraw @a [{"text":""}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"          📦 ","color":"yellow"},{"text":"Compressy","color":"white","bold":true}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  Compress ANY block up to ","color":"gray"},{"text":"32 times!","color":"aqua","bold":true}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  Type ","color":"gray"},{"text":"/cblocks help","color":"green","underlined":true,"click_event":{"action":"run_command","command":"/cblocks help"},"hover_event":{"action":"show_text","value":"Click to run"}},{"text":" for commands","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  How to Use:","color":"yellow","bold":true}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   • ","color":"dark_gray"},{"text":"Place 9 blocks in a 3×3 crafting grid","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   • ","color":"dark_gray"},{"text":"Craft alone to decompress back to 9","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   • ","color":"dark_gray"},{"text":"Works with autocrafters & mod machines!","color":"green"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]

