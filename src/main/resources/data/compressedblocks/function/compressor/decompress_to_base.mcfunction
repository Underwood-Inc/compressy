# ========================================
# Decompress to Base Blocks (Level 1 -> Regular)
# ========================================
# Returns 9 regular blocks from a level 1 compressed block

# Clear the compressed block from inventory
clear @s *[custom_data~{compressed_level:1}] 1

# Give 9 of the base block
$give @s $(block_id) 9

# Effects
playsound minecraft:block.amethyst_block.break master @s ~ ~ ~ 1 0.8
particle minecraft:poof ~ ~1 ~ 0.3 0.3 0.3 0.05 10

tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Decompressed to 9 blocks!","color":"green"}]

