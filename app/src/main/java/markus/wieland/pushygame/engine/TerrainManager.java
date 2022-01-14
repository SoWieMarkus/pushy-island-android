package markus.wieland.pushygame.engine;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.helper.Manager;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.pressure.PressurePlateTerrain;
import markus.wieland.pushygame.engine.terrain.pressure.Teleporter;
import markus.wieland.pushygame.ui.game.PushyFieldView;

public class TerrainManager extends Manager<Terrain, PushyFieldView<Terrain>> {

    private List<PressurePlateTerrain> pressurePlateTerrains;

    public TerrainManager(Matrix<PushyFieldView<Terrain>> pushyFieldViews) {
        super(pushyFieldViews);
        initialize();
    }

    @Override
    public void initialize() {
        pressurePlateTerrains = getOfType(PressurePlateTerrain.class);
    }

    public List<PressurePlateTerrain> getPressurePlateTerrains() {
        return pressurePlateTerrains;
    }

    public Terrain getOtherTeleporter(Teleporter teleporter) {
        for (Teleporter teleporterOfList : getOfType(Teleporter.class)) {
            if (teleporterOfList.equals(teleporter)) continue;
            if (teleporter.getClass().equals(teleporterOfList.getClass())) return teleporterOfList;
        }
        return null;
    }

    public <G extends Terrain> List<G> getSubListOfPressurePlates(Class<G> type) {
        List<G> filteredList = new ArrayList<>();
        for (PressurePlateTerrain pressurePlateTerrain : pressurePlateTerrains) {
            if (type.isInstance(pressurePlateTerrain)) filteredList.add((G) pressurePlateTerrain);
        }
        return filteredList;
    }


}

