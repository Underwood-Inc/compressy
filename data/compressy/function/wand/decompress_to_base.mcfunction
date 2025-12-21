# ========================================
# Decompress to Base (Macro)
# ========================================
# Arguments: block_id

# Get count of Compressy
execute store result score #count cb.temp run data get entity @s Inventory[{Slot:-106b}].count

# Calculate total blocks to give (count * 9)
scoreboard players operation #give_count cb.temp = #count cb.temp
scoreboard players operation #give_count cb.temp *= #9 cb.data

execute store result storage compressy:temp give_count int 1 run scoreboard players get #give_count cb.temp

# Clear offhand
item replace entity @s weapon.offhand with air

# Give base blocks
$give @s $(block_id) $(give_count)

# Effects
playsound minecraft:block.amethyst_block.break master @s ~ ~ ~ 1 0.8
particle minecraft:poof ~ ~1 ~ 0.3 0.3 0.3 0.05 10

$tellraw @s [{"text":"[Compressy] ","color":"gold"},{"text":"Decompressed to $(give_count) blocks!","color":"green"}]

