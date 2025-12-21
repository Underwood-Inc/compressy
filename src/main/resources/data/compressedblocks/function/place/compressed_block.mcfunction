# ========================================
# Place Compressed Block
# ========================================
# Called when a player places a compressed block
# Creates block_display + text_display entities for visual representation
# Arguments: block_id, level from storage

# Summon the base block display (shows the original block texture)
$summon block_display ~ ~ ~ {Tags:["cb.compressed_display","cb.base_display"],block_state:{Name:"$(block_id)"},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[1f,1f,1f]},brightness:{sky:15,block:15}}

# Summon the compression level text display
$summon text_display ~ ~1.2 ~ {Tags:["cb.compressed_display","cb.level_display"],text:'{"text":"×$(level)","color":"gold","bold":true}',billboard:"center",see_through:false,shadow:true,background:0}

# Summon a small marker for interaction detection
summon marker ~ ~ ~ {Tags:["cb.compressed_display","cb.marker"]}

# Play placement sound
playsound minecraft:block.respawn_anchor.charge master @a ~ ~ ~ 0.5 1.5

# Particles based on compression level
$execute if score #level cb.temp matches 1..5 run particle minecraft:happy_villager ~ ~0.5 ~ 0.3 0.3 0.3 0 5
$execute if score #level cb.temp matches 6..10 run particle minecraft:end_rod ~ ~0.5 ~ 0.3 0.3 0.3 0.05 10
$execute if score #level cb.temp matches 11..20 run particle minecraft:dragon_breath ~ ~0.5 ~ 0.3 0.3 0.3 0.02 15
$execute if score #level cb.temp matches 21.. run particle minecraft:reverse_portal ~ ~0.5 ~ 0.3 0.3 0.3 0.1 20

