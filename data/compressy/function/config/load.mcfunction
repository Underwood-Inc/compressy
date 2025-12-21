# ========================================
# Compressy - Config Load
# ========================================
# Initialize configuration in data storage

# Initialize config storage if not exists
execute unless data storage compressy:config {} run data modify storage compressy:config set value {}

# Set defaults if not configured
execute unless data storage compressy:config max_compression_level run data modify storage compressy:config max_compression_level set value 32
execute unless data storage compressy:config require_full_stack run data modify storage compressy:config require_full_stack set value false
execute unless data storage compressy:config show_particles run data modify storage compressy:config show_particles set value true
execute unless data storage compressy:config debug_mode run data modify storage compressy:config debug_mode set value false

# Load config into scoreboards for faster access
execute store result score #max_compression cb.data run data get storage compressy:config max_compression_level
execute store result score #debug_mode cb.data if data storage compressy:config {debug_mode:true}

