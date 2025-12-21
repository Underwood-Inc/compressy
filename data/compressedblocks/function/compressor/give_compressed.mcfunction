# ========================================
# Give Compressed Block (Macro Function)
# ========================================
# Uses macros to dynamically create compressed blocks
# Arguments: block_id, clear_count

# Clear the source blocks
$clear @s $(block_id) $(clear_count)

# Calculate how many compressed blocks to give
$execute store result score #give_count cb.temp run clear @s $(block_id) 0

# Give the compressed block with dynamic NBT
# The block ID is stored in the custom_data along with compression level
$give @s $(block_id)[custom_name='{"text":"Compressed $(block_id)","color":"aqua","italic":false,"bold":true}',lore=['{"text":"Compression Level: 1","color":"yellow","italic":false}','{"text":"Contains: 9 blocks","color":"gray","italic":false}','{"text":"","italic":false}','{"text":"Right-click compressor to compress more!","color":"green","italic":false}'],custom_data={compressed_level:1,compressed_block:"$(block_id)",block_count:9},enchantment_glint_override=true]

# Play success sound and particles
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 1 1.5
particle minecraft:happy_villager ~ ~1 ~ 0.5 0.5 0.5 0 10

tellraw @s [{"text":"[Compressed Blocks] ","color":"gold"},{"text":"Compressed blocks to Level 1!","color":"green"}]

