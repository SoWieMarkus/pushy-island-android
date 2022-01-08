package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Sand extends Terrain {

    private TerrainType terrainType;

    public Sand(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, R.drawable.sand);
        this.terrainType = terrainType;
        switch (terrainType) {
            case SAND_BOTTOM_RIGHT:
                setDrawable(R.drawable.sand_bottom_right);
                break;
            case SAND_TOP_RIGHT:
                setDrawable(R.drawable.sand_top_right);
                break;
            case SAND_TOP_LEFT:
                setDrawable(R.drawable.sand_top_left);
                break;
            case SAND_BOTTOM_LEFT:
                setDrawable(R.drawable.sand_bottom_left);
                break;
            default:
                setDrawable(R.drawable.sand);
                break;
        }
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public TerrainType getType() {
        return terrainType;
    }
}
