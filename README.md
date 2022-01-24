# Pushy-Island

Disclaimer: Based on the game _Pushy Island_ from <a href="http://www.lws9.de/">Lernwerkstatt</a>. No original code was used.

## Features

### New entities not available in the original game

I implemented all entities from the original game. Additionally to that I added some new entities. They are mostly logic gates. The functionallity is explained down below in the tutorial.

### Campaign

All original level can be played in the game. Additionally I added some new level with my new entities to the campaign.

### Editor

With the editor you can create your own level. It supports these functionalities:

- undo / redo
- fill
- erase
- auto smoothing corners
- reset

After you validated your level by completing it on your own you have to option to share your level with your friends. For that you can click on the share button and select the way you want to share your level. Your friend will receive a "code" which they can enter in their app.

#### Level code version 1

Until today there is only one version for this level code. But if I will change the way levels are encoded I will add a documentation for that version here.

In the version 1 the whole string can be interpreted as a byte string in hexadecimal.

The first byte is the version number. So usually it should be "01", which is equal to 1 in decimal. Afterwards there are 12 (height) * 20 (width) * 6 bits (be careful __not__ bytes) representing the terrain, followed by 12 * 20 * 6 bits representing the entities. So you have to decode the byte string in that range to a binary string and split it where the index mod 6 is 0. Those 6 bits represent a terrain type or entity type (depending on the position ofc.). You can see the "value" of a type <a href="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/java/markus/wieland/pushygame/engine/level/TerrainType.java">here (terrain)</a> and <a href="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/java/markus/wieland/pushygame/engine/level/EntityType.java">here (entities)</a>. After 20 tiles a new line begins. So the order is like this:

```javascript
01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20
21 22 23 ...
```

The remaining bytes represent the level name. Every byte represents one character. You get the character but just casting the value of the byte to a char.

## How to play the game

The main goal of the game is to reach the hut on the island. For that you use the arrow keys. But to enter the hut you have to solve all tasks on the island.

### Game mechanics

There are three main height level.
- Water (0)
- Sand (1)
- Grass (2)

You can't walk on water, except you have a *boat*. You can jump from grass to sand, but not from sand to grass. For that you need a spring. Its the same with movable entities. You can push down a entity from grass to sand, but not the other way around.

#### Hut

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/finish.png" alt="finish">

#### Tree

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/tree.png" alt="tree">

#### Stone

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/stone.png" alt="stone">

#### Grass

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/grass.png" alt="grass"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/grass_top_left.png" alt="grass_top_left"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/grass_top_right.png" alt="grass_top_right"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/grass_bottom_left.png" alt="grass_bottom_left"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/grass_bottom_right.png" alt="grass_bottom_right">

#### Sand

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand.png" alt="sand"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_top_left.png" alt="sand_top_left"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_top_right.png" alt="sand_top_right"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_bottom_left.png" alt="sand_bottom_left"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_bottom_right.png" alt="sand_bottom_right">

#### Water

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/water.png" alt="water">

#### Box

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/box.png" alt="box"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/box_water.png" alt="box_water">

- moveable entity
- can be pushed into [water](#water)
- If a box is pushed into the water you can walk on it
- You can also push entities on the box in the water
- With them you can build bridges to other islands
- A box on land destroys [flying stones](#shot) shot from a [slinsghot](#slinsghot). 

#### Sea star
<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/seastar.png" alt="sea_star">

- moveable entitiy
- can be pushed into [water](#water)
- all sea stars on one islands have to pushed into the water before you can finish the level

#### Seed

<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/seed.png" alt="seed">

- collectible entity
- after collecting it you can place the seed on a [farm](#Farm)

#### Bottle
<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/bottle.png" alt="bottle"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/bottle_with_water.png" alt="bottle_with_water">

- movable entity
- if it is pushed onto a [water hole](#Water-hole) the bottle will be filled with water
- if you push a bottle with water on a [farm](#Farm) with a [seed](#Seed) the bottle will disappear and a [spring](#Spring) appears

#### Water hole
<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_with_water.png" alt="water_hole">

#### Farm
<img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/sand_with_farm.png" alt="farm"> <img src="https://github.com/SoWieMarkus/Pushy-Island/blob/main/app/src/main/res/drawable/farm_with_seed.png" alt="farm_with_seed">

- if the player moves on this field and has a [seed](#Seed) in his inventory the farm will turn into a farm with a seed
- if the player then pushes a bottle with water on this farm with a seed a [spring](#Spring) appears


#### Spring




