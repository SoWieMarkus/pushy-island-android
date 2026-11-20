# Tiles

Every level has two layers: **terrain** (the ground) and **objects** standing on it. The id below each name
is the one used in [level files](./level-format/README.md). Tiles marked _max. n_ may only appear that often in a level.

A level is solved when Pushy walks into the hut and every **task** is done.

# Terrain

## Ground

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/sand.png" width="32" alt="sand"> | **Sand**<br>`sand` | Normal ground. |
| <img src="../app/src/main/assets/sprites/grass.png" width="32" alt="grass"> | **Grass**<br>`grass` | Higher ground. Pushy can jump down to sand but not climb up. Flowers grow on grass. |
| <img src="../app/src/main/assets/sprites/sand_top_left.png" width="32" alt="sand_top_left"> <img src="../app/src/main/assets/sprites/sand_top_right.png" width="32" alt="sand_top_right"> <img src="../app/src/main/assets/sprites/sand_bottom_left.png" width="32" alt="sand_bottom_left"> <img src="../app/src/main/assets/sprites/sand_bottom_right.png" width="32" alt="sand_bottom_right"> | **Sand corners**<br>`sand_top_left`, `sand_top_right`, `sand_bottom_left`, `sand_bottom_right` | Rounded sand, otherwise like sand. The editors can set them automatically (_Smooth corners_). |
| <img src="../app/src/main/assets/sprites/grass_top_left.png" width="32" alt="grass_top_left"> <img src="../app/src/main/assets/sprites/grass_top_right.png" width="32" alt="grass_top_right"> <img src="../app/src/main/assets/sprites/grass_bottom_left.png" width="32" alt="grass_bottom_left"> <img src="../app/src/main/assets/sprites/grass_bottom_right.png" width="32" alt="grass_bottom_right"> | **Grass corners**<br>`grass_top_left`, `grass_top_right`, `grass_bottom_left`, `grass_bottom_right` | Rounded grass, otherwise like grass. |
| <img src="../app/src/main/assets/sprites/water.png" width="32" alt="water"> | **Water**<br>`water` | Pushy can't walk on water. Boxes sink into it, sea stars swim away. |
| <img src="../app/src/main/assets/sprites/box_water.png" width="32" alt="box_water"> | **Sunken box**<br>`box_water` | A box that was pushed into the water. Pushy can walk over it. |
| <img src="../app/src/main/assets/sprites/ice.png" width="32" alt="ice"> | **Ice**<br>`ice` | Can be walked on once: as soon as something stood on it and left, it melts into water. |
| <img src="../app/src/main/assets/sprites/hole.png" width="32" alt="hole"> | **Hole**<br>`hole` | Nothing gets past it until it is filled – by a rolling coconut or by the coconut tunnels. |

## Water and travel

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/boat.png" width="32" alt="boat"> <img src="../app/src/main/assets/sprites/boat_east.png" width="32" alt="boat_east"> | **Boat**<br>`boat` | Pushy steps onto the boat and walks towards the water: the boat sails in that direction until the next field is no free water. Its bow turns where it sailed. |
| <img src="../app/src/main/assets/sprites/spring.png" width="32" alt="spring"> | **Vine**<br>`spring` | A springy vine that grows where a full bottle waters a planted farm. Throws Pushy one field further in the direction he walked onto it. |
| <img src="../app/src/main/assets/sprites/teleporter.png" width="32" alt="teleporter"> | **Teleporter**<br>`teleporter` | _max. 2_ – Beams Pushy to the other teleporter. |
| <img src="../app/src/main/assets/sprites/item_teleporter.png" width="32" alt="item_teleporter"> | **Item teleporter**<br>`item_teleporter` | _max. 2_ – Beams anything that is pushed or walks onto it to the other item teleporter. |
| <img src="../app/src/main/assets/sprites/water_invisible.png" width="32" alt="water_invisible"> | **Invisible bridge**<br>`water_invisible` | Looks like water while one of its plates is free, but can always be walked on – the plates just make the path visible. |
| <img src="../app/src/main/assets/sprites/water_invisible_pressure_plate.png" width="32" alt="water_invisible_pressure_plate"> | **Bridge plate**<br>`water_invisible_pressure_plate` | While something stands on every bridge plate, the invisible bridges show up as ground. |

## Plates and goals

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/statue_finish_red.png" width="32" alt="statue_finish_red"> <img src="../app/src/main/assets/sprites/statue_finish_green.png" width="32" alt="statue_finish_green"> <img src="../app/src/main/assets/sprites/statue_finish_blue.png" width="32" alt="statue_finish_blue"> | **Statue plates**<br>`statue_finish_red`, `statue_finish_green`, `statue_finish_blue` | The statue of the same color has to stand here. |
| <img src="../app/src/main/assets/sprites/barrel_finish.png" width="32" alt="barrel_finish"> | **Barrel plate**<br>`barrel_finish` | A barrel has to stand here. |
| <img src="../app/src/main/assets/sprites/flower_red_pressure_plate.png" width="32" alt="flower_red_pressure_plate"> <img src="../app/src/main/assets/sprites/flower_yellow_pressure_plate.png" width="32" alt="flower_yellow_pressure_plate"> | **Flower plates**<br>`flower_red_pressure_plate`, `flower_yellow_pressure_plate` | **Task:** a flower of the same color has to grow on it. Grass. |
| <img src="../app/src/main/assets/sprites/changeable_flower_red.png" width="32" alt="changeable_flower_red"> <img src="../app/src/main/assets/sprites/changeable_flower_green.png" width="32" alt="changeable_flower_green"> | **Flower beds**<br>`changeable_flower_red`, `changeable_flower_green` | **Task:** all flower beds must have the same color. Flipped with the string plates. |
| <img src="../app/src/main/assets/sprites/string_pressure_plate.png" width="32" alt="string_pressure_plate"> | **String plate**<br>`string_pressure_plate` | The first string plate Pushy steps on makes him lay a string behind him; the second one flips the color of every flower bed under the string. Each plate works once. |
| <img src="../app/src/main/assets/sprites/bomb_field.png" width="32" alt="bomb_field"> | **Bomb trigger**<br>`bomb_field` | When Pushy steps on it, every bomb explodes. |
| <img src="../app/src/main/assets/sprites/spikes.png" width="32" alt="spikes"> | **Spikes**<br>`spikes` | Block the way while at least one spike plate is free. They stay down while something stands on them. |
| <img src="../app/src/main/assets/sprites/spike_pressure_plate.png" width="32" alt="spike_pressure_plate"> | **Spike plate**<br>`spike_pressure_plate` | Cover all spike plates to pull the spikes in. |

## Farming and coconuts

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/farm.png" width="32" alt="farm"> | **Farm**<br>`farm` | Pushy plants a seed from his inventory when he walks over it. |
| <img src="../app/src/main/assets/sprites/water_hole.png" width="32" alt="water_hole"> | **Water hole**<br>`water_hole` | Fills bottles pushed onto it. Octopuses brought here go home. |
| <img src="../app/src/main/assets/sprites/coconut_tunnel.png" width="32" alt="coconut_tunnel"> | **Coconut tunnel**<br>`coconut_tunnel` | Coconuts roll through tunnels; a coconut inside a tunnel can only roll on to tunnel fields. |
| <img src="../app/src/main/assets/sprites/coconut_tunnel_finish.png" width="32" alt="coconut_tunnel_finish"> | **Tunnel end**<br>`coconut_tunnel_finish` | When every tunnel end holds a coconut, all holes of the level get filled. |

## Logic

Logic tiles carry signals. Gates switch 0.3 s after their inputs change.

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/cable.png" width="32" alt="cable"> | **Cable**<br>`cable` | Carries a signal between logic parts and glows while active. Connects to neighbouring cables and ports automatically. |
| <img src="../app/src/main/assets/sprites/logic_pressure_plate.png" width="32" alt="logic_pressure_plate"> | **Logic plate**<br>`logic_pressure_plate` | Sends a signal while something stands on it. |
| <img src="../app/src/main/assets/sprites/door.png" width="32" alt="door"> | **Door**<br>`door` | Blocks the way; opens while a signal arrives from any side. |

# Objects

## Pushy and the hut

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/pushy.png" width="32" alt="pushy"> | **Pushy**<br>`pushy` | _max. 1_ – That's you. |
| <img src="../app/src/main/assets/sprites/finish.png" width="32" alt="finish"> | **Hut**<br>`finish` | _max. 1_ – Walk into it from the south to finish the level once every task is done. |

## Obstacles

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/tree.png" width="32" alt="tree"> | **Palm tree**<br>`tree` | Blocks the way. A slingshot stone hitting it drops a coconut behind it (on sand). |
| <img src="../app/src/main/assets/sprites/stone.png" width="32" alt="stone"> | **Rock**<br>`stone` | Blocks the way and slingshot stones. Bombs blow it up. |
| <img src="../app/src/main/assets/sprites/buoy.png" width="32" alt="buoy"> | **Buoy**<br>`buoy` | Blocks the way, usually on water. |
| <img src="../app/src/main/assets/sprites/pirate_hut.png" width="32" alt="pirate_hut"> | **Pirate hut**<br>`pirate_hut` | _max. 1_ – Blocks the way. |

## Things to push

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/box.png" width="32" alt="box"> | **Box**<br>`box` | Can be pushed. Sinks into water and becomes a bridge. |
| <img src="../app/src/main/assets/sprites/barrel.png" width="32" alt="barrel"> | **Barrel**<br>`barrel` | **Task:** every barrel has to stand on a barrel plate. |
| <img src="../app/src/main/assets/sprites/statue_red.png" width="32" alt="statue_red"> <img src="../app/src/main/assets/sprites/statue_green.png" width="32" alt="statue_green"> <img src="../app/src/main/assets/sprites/statue_blue.png" width="32" alt="statue_blue"> | **Statues**<br>`statue_red`, `statue_green`, `statue_blue` | Pushing one statue moves **all** statues of its color in that direction. **Task:** each statue has to stand on its plate. |
| <img src="../app/src/main/assets/sprites/sea_star.png" width="32" alt="sea_star"> | **Sea star**<br>`sea_star` | **Task:** push every sea star back into the water. |
| <img src="../app/src/main/assets/sprites/bomb.png" width="32" alt="bomb"> | **Bomb**<br>`bomb` | Can be pushed. Explodes when Pushy steps on a bomb trigger and destroys rocks next to it. |
| <img src="../app/src/main/assets/sprites/coconut.png" width="32" alt="coconut"> | **Coconut**<br>`coconut` | Rolls until it hits something. Fills the first hole it rolls into. |
| <img src="../app/src/main/assets/sprites/bottle.png" width="32" alt="bottle"> | **Bottle**<br>`bottle` | Gets filled on a water hole. A full bottle pushed onto a planted farm makes a springy vine grow there. |
| <img src="../app/src/main/assets/sprites/shell_1.png" width="32" alt="shell_1"> <img src="../app/src/main/assets/sprites/shell_2.png" width="32" alt="shell_2"> <img src="../app/src/main/assets/sprites/shell_3.png" width="32" alt="shell_3"> | **Mood shells**<br>`shell_1`, `shell_2`, `shell_3` | Get happier with every push (1 → 2 → 3 → 1 …). **Task:** every mood shell has to be happy (3). |
| <img src="../app/src/main/assets/sprites/count_one.png" width="32" alt="count_one"> <img src="../app/src/main/assets/sprites/count_two.png" width="32" alt="count_two"> <img src="../app/src/main/assets/sprites/count_three.png" width="32" alt="count_three"> <img src="../app/src/main/assets/sprites/count_four.png" width="32" alt="count_four"> <img src="../app/src/main/assets/sprites/count_five.png" width="32" alt="count_five"> | **Number blocks**<br>`count_one`, `count_two`, `count_three`, `count_four`, `count_five` | _max. 1 each_ – Start hidden. Pushing one uncovers it. Uncover them in order 1, 2, 3, … – a wrong one hides all of them again. **Task:** uncover all of them. |
| <img src="../app/src/main/assets/sprites/leaf_up.png" width="32" alt="leaf_up"> <img src="../app/src/main/assets/sprites/leaf_right.png" width="32" alt="leaf_right"> <img src="../app/src/main/assets/sprites/leaf_down.png" width="32" alt="leaf_down"> <img src="../app/src/main/assets/sprites/leaf_left.png" width="32" alt="leaf_left"> | **Leaves**<br>`leaf_up`, `leaf_right`, `leaf_down`, `leaf_left` | Can be pushed. Redirect slingshot stones that fly over them. |

## Things that react

Pushy doesn't push these – he walks against them.

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/slingshot.png" width="32" alt="slingshot"> | **Slingshot**<br>`slingshot` | From the south, with a stone in the inventory: fires the stone north. It flies until it hits something (see leaves, trees, shells). |
| <img src="../app/src/main/assets/sprites/leaf_changer_north.png" width="32" alt="leaf_changer_north"> <img src="../app/src/main/assets/sprites/leaf_changer_east.png" width="32" alt="leaf_changer_east"> <img src="../app/src/main/assets/sprites/leaf_changer_south.png" width="32" alt="leaf_changer_south"> <img src="../app/src/main/assets/sprites/leaf_changer_west.png" width="32" alt="leaf_changer_west"> | **Leaf turner**<br>`leaf_changer_north`, `leaf_changer_east`, `leaf_changer_south`, `leaf_changer_west` | Turns every leaf of the level into the direction Pushy walked against it. |
| <img src="../app/src/main/assets/sprites/flower_red.png" width="32" alt="flower_red"> <img src="../app/src/main/assets/sprites/flower_yellow.png" width="32" alt="flower_yellow"> | **Flowers**<br>`flower_red`, `flower_yellow` | Grow flowers of their color across the grass in the direction Pushy walked against them, until something is in the way. |
| <img src="../app/src/main/assets/sprites/chest.png" width="32" alt="chest"> | **Chest**<br>`chest` | From the south, with a key: opens and gives a coin. |
| <img src="../app/src/main/assets/sprites/crab_mother.png" width="32" alt="crab_mother"> | **Crab mother**<br>`crab_mother` | _max. 1_ – From the south: takes the crab babies Pushy carries. **Task:** bring her all of them. |
| <img src="../app/src/main/assets/sprites/pirate.png" width="32" alt="pirate"> | **Pirate**<br>`pirate` | _max. 1_ – From the west: takes Pushy's coins and leaves once he got all of them (one for every coin and every key of the level). **Task:** pay the pirate. |
| <img src="../app/src/main/assets/sprites/tower.png" width="32" alt="tower"> | **Tower**<br>`tower` | Walk against it with a pearl to light it. When every tower is lit, all of them disappear. |
| <img src="../app/src/main/assets/sprites/shell.png" width="32" alt="shell"> <img src="../app/src/main/assets/sprites/shell_open_with_pearl.png" width="32" alt="shell_open_with_pearl"> | **Shell**<br>`shell`, `shell_open_with_pearl` | Closed shells can be pushed. A slingshot stone opens them; walk against an open shell to take its pearl. |
| <img src="../app/src/main/assets/sprites/octopus.png" width="32" alt="octopus"> | **Octopus**<br>`octopus` | Follows Pushy when it is right behind him and can't be pushed. **Task:** lead every octopus to a water hole. |

## Items

Picked up by walking onto them and used automatically.

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/coin.png" width="32" alt="coin"> | **Coin**<br>`coin` | For the pirate. |
| <img src="../app/src/main/assets/sprites/key.png" width="32" alt="key"> | **Key**<br>`key` | Opens a chest. |
| <img src="../app/src/main/assets/sprites/crab_baby.png" width="32" alt="crab_baby"> | **Crab baby**<br>`crab_baby` | For the crab mother. |
| <img src="../app/src/main/assets/sprites/seed.png" width="32" alt="seed"> | **Seed**<br>`seed` | Planted on a farm. |
| <img src="../app/src/main/assets/sprites/shot.png" width="32" alt="shot"> | **Stone**<br>`shot` | Ammunition for the slingshot. |
| <img src="../app/src/main/assets/sprites/pearl.png" width="32" alt="pearl"> | **Pearl**<br>`pearl` | Lights a tower. |
| <img src="../app/src/main/assets/sprites/energy.png" width="32" alt="energy"> | **Energy**<br>`energy` | Pays for one lever pull or button press. |

## Logic objects

Logic objects show their inputs and outputs as small marks on their sides.

| | Tile | What it does |
| --- | --- | --- |
| <img src="../app/src/main/assets/sprites/power_block.png" width="32" alt="power_block"> | **Power block**<br>`power_block` | Can be pushed. Always sends a signal to all sides. |
| <img src="../app/src/main/assets/sprites/lever.png" width="32" alt="lever"> | **Lever**<br>`lever` | Costs one energy to flip. Sends a signal to all sides while it is on. |
| <img src="../app/src/main/assets/sprites/button.png" width="32" alt="button"> | **Button**<br>`button` | Costs one energy to press. Sends a signal for about a second. |
| <img src="../app/src/main/assets/sprites/logic_gate_and.png" width="32" alt="logic_gate_and"> | **AND gate**<br>`logic_gate_and` | Inputs west and east, output north: on while **both** inputs are on. |
| <img src="../app/src/main/assets/sprites/logic_gate_or.png" width="32" alt="logic_gate_or"> | **OR gate**<br>`logic_gate_or` | Inputs west and east, output north: on while **at least one** input is on. |
| <img src="../app/src/main/assets/sprites/logic_gate_xor.png" width="32" alt="logic_gate_xor"> | **XOR gate**<br>`logic_gate_xor` | Inputs west and east, output north: on while **exactly one** input is on. |
| <img src="../app/src/main/assets/sprites/logic_gate_not.png" width="32" alt="logic_gate_not"> | **NOT gate**<br>`logic_gate_not` | Input south, output north: on while the input is **off**. |
| <img src="../app/src/main/assets/sprites/repeater_north_active.png" width="32" alt="repeater_north_active"> <img src="../app/src/main/assets/sprites/repeater_east_active.png" width="32" alt="repeater_east_active"> <img src="../app/src/main/assets/sprites/repeater_south_active.png" width="32" alt="repeater_south_active"> <img src="../app/src/main/assets/sprites/repeater_west_active.png" width="32" alt="repeater_west_active"> | **Repeater**<br>`repeater_north`, `repeater_east`, `repeater_south`, `repeater_west` | Can be pushed. Passes a signal on in one direction only – the direction it was last pushed. |
| <img src="../app/src/main/assets/sprites/lamp.png" width="32" alt="lamp"> | **Lamp**<br>`lamp` | Lights up while a signal arrives from any side. **Task:** every lamp has to be lit. |
| <img src="../app/src/main/assets/sprites/count_down_three.png" width="32" alt="count_down_three"> | **Countdown lamp**<br>`count_down` | Counts down 3 → 0, one step for every new signal, and then stays lit. **Task:** like a lamp. |
