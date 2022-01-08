package markus.wieland.pushygame;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;
import markus.wieland.pushygame.levelbuilder.LevelBuilderItemAdapter;
import markus.wieland.pushygame.ui.PushyGridAdapter;
import markus.wieland.pushygame.ui.PushyView;

public class LevelBuilderActivity extends DefaultActivity {


    private PushyView terrainView;
    private PushyView entityView;

    private LevelBuilder levelBuilder;

    private RecyclerView recyclerViewTerrain;
    private RecyclerView recyclerViewEntity;

    public LevelBuilderActivity() {
        super(R.layout.activity_level_builder);
    }

    @Override
    public void bindViews() {
        terrainView = findViewById(R.id.game_level_builder_terrain);
        entityView = findViewById(R.id.game_level_builder_entities);
        recyclerViewTerrain = findViewById(R.id.activity_level_builder_terrain_items);
        recyclerViewEntity = findViewById(R.id.activity_level_builder_entities_items);
    }

    @Override
    public void initializeViews() {
        terrainView.setNumColumns(LevelBuilder.LEVEL_WIDTH);
        entityView.setNumColumns(LevelBuilder.LEVEL_WIDTH);

        recyclerViewTerrain.setHasFixedSize(true);
        recyclerViewEntity.setHasFixedSize(true);
        recyclerViewTerrain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEntity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }

    @Override
    public void execute() {
        levelBuilder = new LevelBuilder(this);

        LevelBuilderItemAdapter<Entity> levelBuilderEntityAdapter = new LevelBuilderItemAdapter<>(null);
        LevelBuilderItemAdapter<Terrain> levelBuilderTerrainAdapter = new LevelBuilderItemAdapter<>(null);

        levelBuilderEntityAdapter.submitList(LevelBuilder.getEntitiesWhichCanBePlaced());
        levelBuilderTerrainAdapter.submitList(LevelBuilder.getTerrainWhichCanBePlaced());

        recyclerViewTerrain.setAdapter(levelBuilderTerrainAdapter);
        recyclerViewEntity.setAdapter(levelBuilderEntityAdapter);

        terrainView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyTerrainViews()));
        entityView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyEntityViews()));

    }
}