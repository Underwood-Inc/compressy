# ========================================
# Compressy - Config Load
# ========================================
# Initialize configuration in data storage

# Initialize config storage if not exists
execute unless data storage compressedblocks:config {} run data modify storage compressedblocks:config set value {}

# Set defaults if not configured
execute unless data storage compressedblocks:config max_compression_level run data modify storage compressedblocks:config max_compression_level set value 32
execute unless data storage compressedblocks:config require_full_stack run data modify storage compressedblocks:config require_full_stack set value false
execute unless data storage compressedblocks:config show_particles run data modify storage compressedblocks:config show_particles set value true
execute unless data storage compressedblocks:config debug_mode run data modify storage compressedblocks:config debug_mode set value false

# Load config into scoreboards for faster access
execute store result score #max_compression cb.data run data get storage compressedblocks:config max_compression_level
execute store result score #debug_mode cb.data if data storage compressedblocks:config {debug_mode:true}

