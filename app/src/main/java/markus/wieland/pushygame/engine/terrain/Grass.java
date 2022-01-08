package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Grass extends Terrain {

    private final TerrainType terrainType;

    public Grass(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, R.drawable.grass);
        this.terrainType = terrainType;
        switch (terrainType) {
            case GRASS_BOTTOM_RIGHT:
                setDrawable(R.drawable.grass_bottom_right);
                break;
            case GRASS_TOP_RIGHT:
                setDrawable(R.drawable.grass_top_right);
                break;
            case GRASS_TOP_LEFT:
                setDrawable(R.drawable.grass_top_left);
                break;
            case GRASS_BOTTOM_LEFT:
                setDrawable(R.drawable.grass_bottom_left);
                break;
            default:
                setDrawable(R.drawable.grass);
                break;
        }
    }
    @Override
    public int getElevation() {
        return ELEVATION_GRASS;
    }

    @Override
    public TerrainType getType() {
        return terrainType;
    }
}
