# ========================================
# Compressed Blocks - Load Function
# ========================================
# This function runs once when the datapack is loaded
# Supports compression up to 32 levels (9^32 blocks!)

# Create scoreboards for tracking
scoreboard objectives add cb.data dummy
scoreboard objectives add cb.level dummy
scoreboard objectives add cb.temp dummy
scoreboard objectives add cb.cooldown dummy

# Player interaction detection
scoreboard objectives add cb.use_carrot minecraft.used:minecraft.carrot_on_a_stick

# Constants
scoreboard players set #max_level cb.data 32
scoreboard players set #9 cb.data 9
scoreboard players set #1 cb.data 1
scoreboard players set #0 cb.data 0

# Load config
function compressedblocks:config/load

# Display load message
tellraw @a [{"text":""}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"          📦 ","color":"yellow"},{"text":"Compressed Blocks","color":"white","bold":true}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  Compress ANY block up to ","color":"gray"},{"text":"32 times!","color":"aqua","bold":true}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  Type ","color":"gray"},{"text":"/cblocks help","color":"green","underlined":true,"click_event":{"action":"run_command","command":"/cblocks help"},"hover_event":{"action":"show_text","value":"Click to run"}},{"text":" for commands","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  Quick Start:","color":"yellow","bold":true}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   • ","color":"dark_gray"},{"text":"/cblocks give wand","color":"green","underlined":true,"click_event":{"action":"run_command","command":"/cblocks give wand"},"hover_event":{"action":"show_text","value":"Get a Compression Wand"}},{"text":" - Get wand (recommended!)","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   • ","color":"dark_gray"},{"text":"/cblocks give compressor","color":"aqua","underlined":true,"click_event":{"action":"run_command","command":"/cblocks give compressor"},"hover_event":{"action":"show_text","value":"Get a Compressor block"}},{"text":" - Get compressor block","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"  How to Use:","color":"yellow","bold":true}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   1. Hold blocks in offhand","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   2. Right-click with wand to compress","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"},{"text":"   3. Shift+click to decompress","color":"gray"}]
tellraw @a [{"text":"│","color":"gold"}]
tellraw @a [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gold"}]

