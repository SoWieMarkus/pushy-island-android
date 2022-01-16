package markus.wieland.pushygame.engine.terrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.LogicInput;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class Cable extends Sand {

    private boolean active;
    private final List<Direction> directionsOfCable;

    public Cable(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        this.active = false;
        this.directionsOfCable = new ArrayList<>(Arrays.asList(Direction.class.getEnumConstants()));
    }

    public void updateDrawables(Game game) {
        updateDrawables(game.getTerrainManager(), game.getEntityManager());
    }

    public void updateDrawables(LevelBuilder levelBuilder) {
        updateDrawables(levelBuilder.getTerrainManager(), levelBuilder.getEntityManager());
    }

    public void updateDrawables(TerrainManager terrainManager, EntityManager entityManager) {
        this.directionsOfCable.clear();
        for (Direction direction : Direction.class.getEnumConstants()) {
            if (shouldHaveCable(terrainManager, entityManager, direction))
                directionsOfCable.add(direction);
        }
        if (directionsOfCable.isEmpty()) {
            this.directionsOfCable.addAll(Arrays.asList(Direction.class.getEnumConstants()));
        }
        terrainManager.invalidate(this);
    }

    @Override
    public int[] getDrawableList() {
        List<Integer> drawablesAsList = new ArrayList<>();

        for (Direction direction : directionsOfCable) {
            drawablesAsList.add(getDrawableFromDirection(direction));
        }

        int[] drawables = new int[drawablesAsList.size() + 1];
        drawables[0] = R.drawable.sand;
        for (int i = 0; i < drawablesAsList.size(); i++) {
            drawables[i + 1] = drawablesAsList.get(i);
        }
        return drawables;
    }

    public int getDrawableFromDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                return active ? R.drawable.cable_north_active : R.drawable.cable_north;

            case EAST:
                return active ? R.drawable.cable_east_active : R.drawable.cable_east;

            case SOUTH:
                return active ? R.drawable.cable_south_active : R.drawable.cable_south;

            default:
                return active ? R.drawable.cable_west_active : R.drawable.cable_west;
        }
    }

    public boolean shouldHaveCable(TerrainManager terrainManager, EntityManager entityManager, Direction direction) {
        Coordinate coordinate = getCoordinate().getNextCoordinate(direction);
        if (entityManager.isNotInsideField(coordinate)) return false;
        Entity entity = entityManager.getObject(coordinate);
        Terrain terrain = terrainManager.getObject(coordinate);
        if (terrain instanceof Cable) return true;
        if (terrain instanceof LogicOutput && ((LogicOutput) terrain).isOutput(direction)) return true;
        if (entity instanceof LogicOutput && ((LogicOutput) entity).isOutput(direction)) return true;
        if (entity instanceof LogicInput && ((LogicInput) entity).isInput(direction)) return true;
        return terrain instanceof LogicInput && ((LogicInput) terrain).isInput(direction);
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
