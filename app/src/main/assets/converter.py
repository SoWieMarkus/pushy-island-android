import os
import json

path_assets = os.path.join(os.path.dirname(__file__), 'assets')

print(os.listdir("level"))

#for path in os.listdir():
#    if path == "converter.py":
#        continue
#    with open(path) as json_file:
#        data = json.load(json_file)
#        changed = False
#        for x in range(0,12):
#            for y in range(0,19):
#                terrain = data["terrain"][x][y]
#                entity = data["entities"][x][y]
#                if terrain == "buoy":
#                    data["entities"][x][y] = "buoy"
#                    data["terrain"][x][y] = "water"
#                    changed = True
#        if changed:
#            with open(path, 'w') as outfile:
#                outfile.write(json.dumps(data))
          
                    
                
