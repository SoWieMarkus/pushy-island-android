import os
import json
import re          


def changeEntityTerrain():
    for path in os.listdir("level"):
        path = os.path.join(os.path.dirname(__file__), "level\\" + path)
        if path == "converter.py":
            continue
        with open(path) as json_file:
            data = json.load(json_file)
            changed = False
            for x in range(0,12):
                for y in range(0,20):
                    terrain = data["terrain"][x][y]
                    entity = data["entities"][x][y]
                    if terrain == "buoy":
                        print("YEP")
                        print(x)
                        print(y)
                        data["entities"][x][y] = "buoy"
                        data["terrain"][x][y] = "water"
                        changed = True
        if changed:
                with open(path, 'w') as outfile:
                    outfile.write(json.dumps(data))
entities = {     
    "no_entity":5,
    "tree": 0,
    "box": 1,
    "pushy": 2, 
    "finish": 3, 
    "stone": 4, 
    "seastar": 6, 
    "seed": 7, 
    "bottle": 8, 
    "statue_red": 9, 
    "statue_green": 11, 
    "statue_blue": 10, 
    "bomb": 12, 
    "crab_baby": 13, 
    "crab_mother": 14,
    "slingshot": 15, 
    "shot": 16, 
    "coconut": 17, 
    "leaf_down": 18, 
    "leaf_up": 19, 
    "leaf_left": 20, 
    "leaf_right": 21, 
    "pirat": 22, 
    "pirat_hut": 23, 
    "buoy": 24, 
    "chest": 25, 
    "coin": 26, 
    "key": 27, 
    "tower": 28,
    "pearl": 29, 
    "shell": 30, 
    "shell_open_with_pearl": 31,
    "count_one": 32,
    "count_two": 33, 
    "count_three": 34, 
    "count_four": 35, 
    "count_five": 36, 
    "flower": 37, 
    "flower_red": 38, 
    "shell_1": 39, 
    "shell_2": 40, 
    "shell_3": 41, 
    "octopus": 42, 
    "barrel": 43, 
    "leaf_changer_east": 44, 
    "leaf_changer_west": 45, 
    "leaf_changer_north": 46,
    "leaf_changer_south": 47, 
    "logic_gate_and": 48, 
    "logic_gate_or": 49,
    "logic_gate_xor": 50, 
    "logic_gate_lamp": 51, 
    "lever":52,
    "power_block":53,
    "power_block":54, 
    "energy":55, 
    "button":56, 
    "count_down":57, 
    "repeater_north":58, 
    "repeater_south":59, 
    "repeater_west":60, 
    "repeater_east":61, 
}

terrains = {
    "water": 0, 
    "box_water": 1, 
    "sand": 2, 
    "sand_top_left": 3,
    "sand_top_right": 4, 
    "sand_bottom_left": 5, 
    "sand_bottom_right": 6, 
    "grass": 7, 
    "grass_top_left": 8, 
    "grass_top_right": 9, 
    "grass_bottom_left": 10,
    "grass_bottom_right": 11, 
    "spring": 12,
    "sand_with_water": 13, 
    "sand_with_farm": 14, 
    "statue_finish_red": 15, 
    "statue_finish_green": 17, 
    "statue_finish_blue": 16, 
    "bomb_field": 18, 
    "hole": 19, 
    "spike_pressure_plate": 20, 
    "spikes": 21,
    "boat": 22, 
    "teleporter": 23,
    "water_invisible_pressure_plate": 25,
    "water_invisible": 26, 
    "coconut_tunnel": 27, 
    "coconut_tunnel_finish": 28, 
    "item_teleporter": 29, 
    "ice": 30, 
    "flower_red_pressure_plate": 31, 
    "flower_pressure_plate": 32, 
    "string_pressure_plate": 33, 
    "changable_flower_red": 34, 
    "changable_flower_green": 35, 
    "barrel_finish": 36,
    "cable": 37, 
    "logic_pressure_plate": 38,
    "door": 39,
}


def addRedundandZero(value, places):
    value = str(value)
    while len(value) < places:
        value = "0" + value
    return value

def getBinaryString(value):
    return "{:08b}".format(value)

def getBinary6BitsString(value):
    return "{:06b}".format(value)

print("Levelname".encode("ascii"))
print(getBinaryString(42))


def binaryLevelname(levelname):
    return "".join(f"{ord(i):08b}" for i in levelname)

print(getBinaryString(65)+getBinaryString(66)+ getBinaryString(67))
print(binaryLevelname("ABC"))

def createLevelCode(): 
    for path in os.listdir("level"):
        path = os.path.join(os.path.dirname(__file__), "level\\" + path)
        with open(path) as json_file:
            data = json.load(json_file)
            
            version = getBinaryString(1)
            
            terrainBinary = ""
            entityBinary = ""
            
            for x in range(0,12):
                for y in range(0,20):
                    terrain = data["terrain"][x][y]
                    entity = data["entities"][x][y]
                    
                    terrainBinary += getBinary6BitsString(terrains[terrain])
                    entityBinary += getBinary6BitsString(entities[entity])
            
            levelname = binaryLevelname(data["name"])
            print(data["name"])
            
            binary = version + terrainBinary + entityBinary + levelname
            print(binary)
            hstr = '%0*X' % ((len(binary) + 3) // 4, int(binary, 2))
            print(hstr)
            data["code"] = hstr;
        with open(path, 'w') as outfile:
            outfile.write(json.dumps(data))
 
            
createLevelCode()   
            

