const WATER = "water";
const COUNT_HIDDEN = "count_hidden";
const COUNT_ONE = "count_one";
const COUNT_TWO = "count_two";
const COUNT_THREE = "count_three";
const COUNT_FOUR = "count_four";
const COUNT_FIVE = "count_five";
const COCONUT_TUNNEL = "coconut_tunnel";
const COCONUT_TUNNEL_FINISH = "coconut_tunnel_finish";

const PUSHY = "pushy";
const SHOT = "shot";
const SHELL = "shell";
const HOLE = "hole";
const SLING_SHOT = "slingshot";
const CRAB_BABY = "crab_baby";
const CRAB_MOTHER = "crab_mother";
const SEASTAR = "seastar";
const COIN = "coin";
const FINISH = "finish";
const BOX = "box";
const BOMB = "bomb";
const BOMB_FIELD = "bomb_field";
const BOTTLE = "bottle";
const WATER_BOTTLE = "bottle_with_water";
const SEED = "seed";
const STONE = "stone";
const TREE = "tree";
const STATUE_RED = "statue_red";
const WATER_WITH_BOX = "box_water";
const STATUE_GREEN = "statue_green";
const STATUE_BLUE = "statue_blue";
const STATUE_GOAL_RED = "statue_finish_red";
const STATUE_GOAL_GREEN = "statue_finish_green";
const STATUE_GOAL_BLUE = "statue_finish_blue";
const NO_ENTITY = "no_entity";
const ITEM_TELEPORTER = "item_teleporter";
const BOAT = "boat";
const KEY = "key";
const CHEST = "chest";
const PIRATE = "pirat";
const PIRATE_HUT = "pirat_hut";
const SAND = "sand";
const BUOY = "buoy";
const SPIKES = "spikes";
const TELEPORTER = "teleporter";
const SPIKES_PRESSURE_PLATE = "spike_pressure_plate";
const LEAF_DOWN = "leaf_down";
const LEAF_UP = "leaf_up";
const LEAF_LEFT = "leaf_left";
const LEAF_RIGHT = "leaf_right";
const SPRING = "spring";
const GRASS = "grass";
const ICE = "ice";
const WATER_INVISIBLE = "water_invisible";
const WATER_INVISIBLE_PRESSURE = "water_invisible_pressure_plate";
const GRASS_TOP_LEFT = "grass_top_left";
const GRASS_TOP_RIGHT = "grass_top_right";
const GRASS_BOTTOM_LEFT = "grass_bottom_left";
const GRASS_BOTTOM_RIGHT = "grass_bottom_right";
const SAND_TOP_LEFT = "sand_top_left";
const SAND_TOP_RIGHT = "sand_top_right";
const SAND_BOTTOM_LEFT = "sand_bottom_left";
const SAND_BOTTOM_RIGHT = "sand_bottom_right";
const SAND_WITH_FARM = "sand_with_farm";
const SAND_WITH_WATER = "sand_with_water";
const FARM_WITH_SEED = "farm_with_seed";
const TOWER = "tower";
const FLOWER = "flower";
const PEARL = "pearl";
const COCONUT = "coconut";
const FLOWER_RED = "flower_red";
const FLOWER_RED_PRESSURE_PLATE = "flower_red_pressure_plate";
const FLOWER_PRESSURE_PLATE = "flower_pressure_plate";
const LEAF_CHANGER_NORTH = "leaf_changer_north";
const LEAF_CHANGER_SOUTH = "leaf_changer_south";
const LEAF_CHANGER_EAST = "leaf_changer_east";
const LEAF_CHANGER_WEST = "leaf_changer_west";
const SHELL_1 = "shell_1";
const SHELL_2 = "shell_2";
const SHELL_3 = "shell_3";
const BARREL = "barrel";
const BARREL_FINISH = "barrel_finish";
const STRING_PRESSURE_PLATE = "string_pressure_plate";
const CHANGEABLE_FLOWER_RED = "changable_flower_red";
const CHANGEABLE_FLOWER_GREEN = "changable_flower_green";
const OCTOPUS = "octopus";

let ALL_TERRAIN = [STRING_PRESSURE_PLATE,CHANGEABLE_FLOWER_RED, CHANGEABLE_FLOWER_GREEN, BARREL_FINISH, FLOWER_RED_PRESSURE_PLATE, FLOWER_PRESSURE_PLATE, COCONUT_TUNNEL, COCONUT_TUNNEL_FINISH, WATER_INVISIBLE,WATER_INVISIBLE_PRESSURE, ICE, ITEM_TELEPORTER, BUOY, BOAT, SPIKES_PRESSURE_PLATE, TELEPORTER, SPIKES, HOLE, WATER_WITH_BOX, SAND_WITH_WATER, SPRING, BOMB_FIELD, STATUE_GOAL_RED, STATUE_GOAL_BLUE, STATUE_GOAL_GREEN, FARM_WITH_SEED, SAND_WITH_FARM, WATER, SAND, GRASS, GRASS_TOP_LEFT, GRASS_TOP_RIGHT, GRASS_BOTTOM_LEFT, GRASS_BOTTOM_RIGHT, SAND_TOP_LEFT, SAND_TOP_RIGHT, SAND_BOTTOM_LEFT, SAND_BOTTOM_RIGHT,];
let ALL_ENTITIES = [OCTOPUS, SHELL_1, SHELL_2, SHELL_3,BARREL, LEAF_CHANGER_NORTH, LEAF_CHANGER_SOUTH, LEAF_CHANGER_EAST, LEAF_CHANGER_WEST, FLOWER_RED, COCONUT, PEARL, FLOWER,SHELL, TOWER, COUNT_HIDDEN, COUNT_ONE,  COUNT_TWO, COUNT_THREE, COUNT_FOUR, COUNT_FIVE, KEY, CHEST, PIRATE, PIRATE_HUT, LEAF_DOWN, LEAF_UP, LEAF_LEFT, LEAF_RIGHT, SHOT, SLING_SHOT, STATUE_RED, CRAB_MOTHER, BOMB, STATUE_GREEN, STATUE_BLUE, WATER_BOTTLE, BOTTLE, PUSHY, SEED, BOX, FINISH, TREE, STONE, CRAB_BABY, SEASTAR, COIN, NO_ENTITY];

let currentTool = "water";
let isEntity = false;

let terrain = [];
let entities = [];

function addButton(tileName, entity = false) {
    let toolbar = document.getElementById("tools");
    let tool = document.createElement("button");
    tool.className = "field";
    tool.onclick = function () {
        currentTool = tileName;
        isEntity = entity;
    };

    let image = "url('../app/src/main/res/drawable/" + tileName + ".png')";
    console.log(image);
    tool.style.backgroundImage = image;
    toolbar.appendChild(tool);
}

function create() {
    let height = parseInt(document.getElementById("height").value);
    let width = parseInt(document.getElementById("width").value);

    document.getElementById("name").value = "";
    document.getElementById("number").value = "";

    terrain = [];
    entities = [];

    let terrainElement = document.getElementById("terrain");
    let entitiesElement = document.getElementById("entity");

    fillFields(terrainElement, height, width);
    fillFields(entitiesElement, height, width, true);
}

function createEmptyField(entity, background = undefined) {
    let field = document.createElement("div");
    field.className = "field" + (entity ? " entity" : "");
    if (background !== undefined) field.style.backgroundImage = "url('../app/src/main/res/drawable/" + background + ".png')"
    return field;
}

function fillFields(board, height, width, entity = false) {
    board.innerHTML = "";
    let topRow = document.createElement("div");
    topRow.style.overflow = "auto";

    for (let y = 0; y < width + 1; y++) {
        let number = createEmptyField(false);
        if (y > 0 && !entity) {
            number.innerText = String.fromCharCode(y + 64) + "";
        }
        topRow.appendChild(number);
    }

    board.appendChild(topRow);

    for (let x = 0; x < height; x++) {

        if (entity) {
            entities.push([]);
        } else {
            terrain.push([]);
        }

        let row = document.createElement("div");
        row.style.overflow = "auto";

        let number = createEmptyField(false);
        if (!entity) {
            number.innerText = (x + 1) + "";
        }

        row.appendChild(number);


        for (let y = 0; y < width; y++) {

            if (entity) {
                entities[x].push(NO_ENTITY);
            } else {
                terrain[x].push(WATER);
            }

            let field = createEmptyField(entity, entity ? NO_ENTITY : WATER);
            field.id = buildId(entity, x, y)

            if (entity) {

                field.onmousedown = function () {
                    setValue(x, y);
                };

                field.addEventListener("mouseover", function (e) {
                    if (e.buttons === 1 || e.buttons === 3) {
                        setValue(x, y);
                    }
                });
            }

            row.appendChild(field);
        }
        board.appendChild(row);
    }


}

function setValue(x, y) {
    console.log("SETTING VALUE")
    document.getElementById(buildId(isEntity, x, y)).style.backgroundImage = "url('../app/src/main/res/drawable/" + currentTool + ".png')";
    if (isEntity) {
        entities[x][y] = currentTool;
        if (currentTool === PIRATE_HUT) {
            document.getElementById(buildId(isEntity, x - 1, y)).style.backgroundImage = "url('../app/src/main/res/drawable/" + PIRATE + ".png')";
            entities[x - 1][y] = PIRATE;
        }
        if (currentTool === PIRATE) {
            document.getElementById(buildId(isEntity, x + 1, y)).style.backgroundImage = "url('../app/src/main/res/drawable/" + PIRATE_HUT + ".png')";
            entities[x + 1][y] = PIRATE_HUT;
        }
    } else {
        terrain[x][y] = currentTool;

    }
}

function buildId(entity, x, y) {
    return (entity ? "ENTITY" : "TERRAIN") + "_" + x + "_" + y;
}

function save() {
    if (!validate()) return;

    let level = {}
    level.name = document.getElementById("name").value;
    level.number = parseInt(document.getElementById("number").value);
    level.terrain = terrain;
    level.entities = entities;
    level.width = parseInt(document.getElementById("width").value);
    level.height = parseInt(document.getElementById("height").value);

    let data = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(level));
    let download = document.getElementById("download");
    download.setAttribute("href", data);
    download.setAttribute("download", addLeadingZero(level.number) + "_" + level.name + ".json");
    download.click();
}

function addLeadingZero(number) {
    if (number < 10) return "00" + number;
    if (number < 100) return "0" + number;
    return number;
}


function validate() {
    let pushy = 0;
    let finish = 0;

    let height = parseInt(document.getElementById("height").value);
    let width = parseInt(document.getElementById("width").value);
    let name = document.getElementById("name").value;
    let number = parseInt(document.getElementById("number").value);

    for (let i = 0; i < height; i++) {
        for (let j = 0; j < width; j++) {
            if (entities[i][j] === PUSHY) pushy++;
            if (entities[i][j] === FINISH) finish++;

        }
    }

    if (pushy !== 1) {
        alert("Please place only one pushy!");
        return false;
    }

    if (finish !== 1) {
        alert("Please place only one finish!");
        return false;
    }

    if (name === "") {
        alert("Please enter a level name");
        return false;
    }

    if (number === 0 || isNaN(number)) {
        alert("Please enter a level number");
        return false;
    }

    return true;


}

function init() {

    for (let terrainElement of ALL_TERRAIN) {
        addButton(terrainElement)
    }

    for (let entity of ALL_ENTITIES) {
        addButton(entity, true)
    }

    create();
}