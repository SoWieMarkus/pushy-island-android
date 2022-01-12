package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.terrain.Grass;
import markus.wieland.pushygame.engine.terrain.Sand;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.Water;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class SmoothTask extends MultipleTask {


    public SmoothTask(LevelBuilder levelBuilder) {
        super(levelBuilder);
    }

    private TerrainType checkSand(Coordinate coordinate) {
        Terrain top = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.NORTH));
        Terrain left = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.WEST));
        Terrain right = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.EAST));
        Terrain down = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.SOUTH));

        TerrainType terrainType = null;
        if (!isSandOrGrass(top) && !isSandOrGrass(left) && isSandOrGrass(right) && isSandOrGrass(down)) {
            terrainType = TerrainType.SAND_TOP_LEFT;
        }
        if (!isSandOrGrass(top) && !isSandOrGrass(right) && isSandOrGrass(left) && isSandOrGrass(down)) {
            terrainType = TerrainType.SAND_TOP_RIGHT;
        }
        if (!isSandOrGrass(down) && !isSandOrGrass(left) && isSandOrGrass(right) && isSandOrGrass(top)) {
            terrainType = TerrainType.SAND_BOTTOM_LEFT;
        }
        if (!isSandOrGrass(down) && !isSandOrGrass(right) && isSandOrGrass(left) && isSandOrGrass(top)) {
            terrainType = TerrainType.SAND_BOTTOM_RIGHT;
        }

        return terrainType;
    }

    private TerrainType checkGrass(Coordinate coordinate) {
        Terrain top = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.NORTH));
        Terrain left = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.WEST));
        Terrain right = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.EAST));
        Terrain down = getLevelBuilder().getTerrainManager().getObject(coordinate.getNextCoordinate(Direction.SOUTH));

        TerrainType terrainType = null;
        if (!isSand(top) && !isSand(left) && isSand(right) && isSand(down)) {
            terrainType = TerrainType.GRASS_BOTTOM_RIGHT;
        }
        if (!isSand(top) && !isSand(right) && isSand(left) && isSand(down)) {
            terrainType = TerrainType.GRASS_BOTTOM_LEFT;
        }
        if (!isSand(down) && !isSand(left) && isSand(right) && isSand(top)) {
            terrainType = TerrainType.GRASS_TOP_RIGHT;
        }
        if (!isSand(down) && !isSand(right) && isSand(left) && isSand(top)) {
            terrainType = TerrainType.GRASS_TOP_LEFT;
        }
        return terrainType;

    }

    private boolean isSand(Terrain terrain) {
        if (terrain == null) return true;
        return terrain instanceof Sand || terrain instanceof Water;
    }

    private boolean isSandOrGrass(Terrain terrain) {
        if (terrain == null) return false;
        if (terrain instanceof Grass) return true;
        return terrain instanceof Sand;
    }

    @Override
    public void execute() {
        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                checkForSmooth(coordinate);
            }
        }
    }

    private void checkForSmooth(Coordinate coordinate) {
        Terrain terrain = getLevelBuilder().getTerrainManager().getObject(coordinate);
        if (!(terrain.getClass().equals(Grass.class) || terrain.getClass().equals(Sand.class))) return;
        TerrainType terrainType = null;
        if (terrain instanceof Grass) {
            terrainType = checkGrass(coordinate);
            if (terrainType == null) terrainType = TerrainType.GRASS;
        }
        if (terrain instanceof Sand) {
            terrainType = checkSand(coordinate);
            if (terrainType == null) terrainType = TerrainType.SAND;
        }

        executeSubTask(new SetTask(getLevelBuilder(), coordinate, terrainType));
    }
}
