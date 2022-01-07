package markus.wieland.pushygame.engine.level;

import androidx.constraintlayout.helper.widget.Flow;

import markus.wieland.pushygame.engine.entity.collectible.Coin;
import markus.wieland.pushygame.engine.entity.collectible.Key;
import markus.wieland.pushygame.engine.entity.collectible.Pearl;
import markus.wieland.pushygame.engine.entity.collectible.Seed;
import markus.wieland.pushygame.engine.entity.interactable.Chest;
import markus.wieland.pushygame.engine.entity.interactable.CrabMama;
import markus.wieland.pushygame.engine.entity.interactable.Finish;
import markus.wieland.pushygame.engine.entity.interactable.Flower;
import markus.wieland.pushygame.engine.entity.interactable.Pirate;
import markus.wieland.pushygame.engine.entity.interactable.SlingShot;
import markus.wieland.pushygame.engine.entity.interactable.Tower;
import markus.wieland.pushygame.engine.entity.movable.Barrel;
import markus.wieland.pushygame.engine.entity.movable.Bomb;
import markus.wieland.pushygame.engine.entity.movable.Bottle;
import markus.wieland.pushygame.engine.entity.movable.Box;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Coconut;
import markus.wieland.pushygame.engine.entity.movable.Count;
import markus.wieland.pushygame.engine.entity.movable.CrabBaby;
import markus.wieland.pushygame.engine.entity.movable.Leaf;
import markus.wieland.pushygame.engine.entity.movable.Octopus;
import markus.wieland.pushygame.engine.entity.movable.PushShell;
import markus.wieland.pushygame.engine.entity.statics.PirateHut;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.entity.movable.SeaStar;
import markus.wieland.pushygame.engine.entity.movable.Shell;
import markus.wieland.pushygame.engine.entity.movable.Shot;
import markus.wieland.pushygame.engine.entity.movable.Statue;
import markus.wieland.pushygame.engine.entity.statics.Stone;
import markus.wieland.pushygame.engine.entity.statics.Tree;
import markus.wieland.pushygame.engine.exceptions.UnknownTileException;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.terrain.BarrelFinish;
import markus.wieland.pushygame.engine.terrain.Boat;
import markus.wieland.pushygame.engine.terrain.ChangeableFlower;
import markus.wieland.pushygame.engine.terrain.CoconutTunnel;
import markus.wieland.pushygame.engine.terrain.CoconutTunnelFinish;
import markus.wieland.pushygame.engine.terrain.FlowerFinish;
import markus.wieland.pushygame.engine.terrain.Ice;
import markus.wieland.pushygame.engine.terrain.InvisibleWater;
import markus.wieland.pushygame.engine.terrain.pressure.ItemTeleporter;
import markus.wieland.pushygame.engine.terrain.pressure.BombField;
import markus.wieland.pushygame.engine.terrain.BoxWater;
import markus.wieland.pushygame.engine.terrain.Buoy;
import markus.wieland.pushygame.engine.terrain.Farm;
import markus.wieland.pushygame.engine.terrain.Grass;
import markus.wieland.pushygame.engine.terrain.Hole;
import markus.wieland.pushygame.engine.terrain.Sand;
import markus.wieland.pushygame.engine.terrain.pressure.SpikePressurePlate;
import markus.wieland.pushygame.engine.terrain.Spikes;
import markus.wieland.pushygame.engine.terrain.pressure.Spring;
import markus.wieland.pushygame.engine.terrain.StatueFinish;
import markus.wieland.pushygame.engine.terrain.pressure.StringPressurePlate;
import markus.wieland.pushygame.engine.terrain.pressure.Teleporter;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.Water;
import markus.wieland.pushygame.engine.terrain.WaterHole;
import markus.wieland.pushygame.engine.terrain.pressure.InvisibleWaterPressurePlate;

public class TileMapBuilder {

    private TileMapBuilder() {
    }

    public static Terrain build(TerrainType terrainType, Coordinate coordinate) {
        switch (terrainType) {
            case SAND:
            case SAND_BOTTOM_LEFT:
            case SAND_TOP_LEFT:
            case SAND_TOP_RIGHT:
            case SAND_BOTTOM_RIGHT:
                return new Sand(coordinate, terrainType);
            case GRASS:
            case GRASS_TOP_LEFT:
            case GRASS_TOP_RIGHT:
            case GRASS_BOTTOM_RIGHT:
            case GRASS_BOTTOM_LEFT:
                return new Grass(coordinate, terrainType);
            case WATER:
                return new Water(coordinate);
            case BOX_WATER:
                return new BoxWater(coordinate);

            case SPRING:
                return new Spring(coordinate);
            case WATER_HOLE:
                return new WaterHole(coordinate);
            case FARM:
                return new Farm(coordinate);

            case STATUE_FINISH_BLUE:
            case STATUE_FINISH_GREEN:
            case STATUE_FINISH_RED:
                return new StatueFinish(coordinate, terrainType);

            case BOMB_FIELD:
                return new BombField(coordinate);

            case HOLE:
                return new Hole(coordinate);

            case SPIKES:
                return new Spikes(coordinate);
            case SPIKE_PRESSURE_PLATE:
                return new SpikePressurePlate(coordinate);
            case BUOY:
                return new Buoy(coordinate);
            case BOAT:
                return new Boat(coordinate);
            case TELEPORTER:
                return new Teleporter(coordinate);
            case ITEM_TELEPORTER:
                return new ItemTeleporter(coordinate);
            case WATER_INVISIBLE:
                return new InvisibleWater(coordinate);
            case WATER_INVISIBLE_PRESSURE_PLATE:
                return new InvisibleWaterPressurePlate(coordinate);

            case COCONUT_TUNNEL:
                return new CoconutTunnel(coordinate);
            case COCONUT_TUNNEL_FINISH:
                return new CoconutTunnelFinish(coordinate);

            case ICE:
                return new Ice(coordinate);

            case FLOWER_RED_PRESSURE_PLATE:
            case FLOWER_YELLOW_PRESSURE_PLATE:
                return new FlowerFinish(coordinate, terrainType);

            case STRING_PRESSURE_PLATE:
                return new StringPressurePlate(coordinate);

            case CHANGEABLE_FLOWER_GREEN:
            case CHANGEABLE_FLOWER_RED:
                return new ChangeableFlower(coordinate, terrainType);

            case BARREL_FINISH:
                return new BarrelFinish(coordinate);
            default:
                throw new UnknownTileException(terrainType);
        }
    }

    public static Entity build(EntityType entityType, Coordinate coordinate) {
        switch (entityType) {
            case STONE:
                return new Stone(coordinate);
            case FINISH:
                return new Finish(coordinate);
            case PUSHY:
                return new Pushy(coordinate);
            case BOX:
                return new Box(coordinate);
            case TREE:
                return new Tree(coordinate);
            case NO_ENTITY:
                return null;
            case SEA_STAR:
                return new SeaStar(coordinate);
            case SEED:
                return new Seed(coordinate);
            case BOTTLE:

                return new Bottle(coordinate);
            case STATUE_BLUE:
            case STATUE_GREEN:
            case STATUE_RED:
                return new Statue(coordinate, entityType);

            case BOMB:
                return new Bomb(coordinate);

            case CRAB_MOTHER:
                return new CrabMama(coordinate);
            case CRAB:
                return new CrabBaby(coordinate);
            case SHOT:
                return new Shot(coordinate);
            case COCONUT:
                return new Coconut(coordinate);
            case SLING_SHOT:
                return new SlingShot(coordinate);
            case LEAF_DOWN:
            case LEAF_UP:
            case LEAF_LEFT:
            case LEAF_RIGHT:
                return new Leaf(coordinate, entityType);

            case COIN:
                return new Coin(coordinate);
            case KEY:
                return new Key(coordinate);
            case CHEST:
                return new Chest(coordinate);
            case PIRATE:
                return new Pirate(coordinate);
            case PIRATE_HUT:
                return new PirateHut(coordinate);
            case PEARL:
                return new Pearl(coordinate);
            case TOWER:
                return new Tower(coordinate);
            case SHELL:
                return new Shell(coordinate);

            case FLOWER_RED:
            case FLOWER_YELLOW:
                return new Flower(coordinate, entityType);

            case COUNT_ONE:
            case COUNT_TWO:
            case COUNT_THREE:
            case COUNT_FOUR:
            case COUNT_FIVE:
                return new Count(coordinate,  entityType);

            case SHELL_1:
            case SHELL_2:
            case SHELL_3:
                return new PushShell(coordinate, entityType);

            case BARREL:
                return new Barrel(coordinate);
            case OCTOPUS:
                return new Octopus(coordinate);
            default:
                throw new UnknownTileException(entityType);
        }
    }


}
