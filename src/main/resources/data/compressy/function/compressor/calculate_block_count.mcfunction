# ========================================
# Calculate Block Count for Compression Level
# ========================================
# Calculates 9^level and stores it for display
# For levels > 10, we use scientific notation strings

# Get the level
execute store result score #level cb.temp run data get storage compressy:temp new_level

# Pre-calculated values for common levels (9^n)
# Level 1: 9
# Level 2: 81
# Level 3: 729
# Level 4: 6,561
# Level 5: 59,049
# Level 6: 531,441
# Level 7: 4,782,969
# Level 8: 43,046,721
# Level 9: 387,420,489
# Level 10: 3,486,784,401
# Beyond this, we use approximations

execute if score #level cb.temp matches 1 run data modify storage compressy:temp block_count_str set value "9"
execute if score #level cb.temp matches 2 run data modify storage compressy:temp block_count_str set value "81"
execute if score #level cb.temp matches 3 run data modify storage compressy:temp block_count_str set value "729"
execute if score #level cb.temp matches 4 run data modify storage compressy:temp block_count_str set value "6,561"
execute if score #level cb.temp matches 5 run data modify storage compressy:temp block_count_str set value "59,049"
execute if score #level cb.temp matches 6 run data modify storage compressy:temp block_count_str set value "531,441"
execute if score #level cb.temp matches 7 run data modify storage compressy:temp block_count_str set value "4.78M"
execute if score #level cb.temp matches 8 run data modify storage compressy:temp block_count_str set value "43M"
execute if score #level cb.temp matches 9 run data modify storage compressy:temp block_count_str set value "387M"
execute if score #level cb.temp matches 10 run data modify storage compressy:temp block_count_str set value "3.49B"
execute if score #level cb.temp matches 11 run data modify storage compressy:temp block_count_str set value "31.4B"
execute if score #level cb.temp matches 12 run data modify storage compressy:temp block_count_str set value "282B"
execute if score #level cb.temp matches 13 run data modify storage compressy:temp block_count_str set value "2.54T"
execute if score #level cb.temp matches 14 run data modify storage compressy:temp block_count_str set value "22.9T"
execute if score #level cb.temp matches 15 run data modify storage compressy:temp block_count_str set value "206T"
execute if score #level cb.temp matches 16..20 run data modify storage compressy:temp block_count_str set value "Quadrillions+"
execute if score #level cb.temp matches 21..25 run data modify storage compressy:temp block_count_str set value "Sextillions+"
execute if score #level cb.temp matches 26..30 run data modify storage compressy:temp block_count_str set value "Octillions+"
execute if score #level cb.temp matches 31..32 run data modify storage compressy:temp block_count_str set value "∞ (Astronomical)"

