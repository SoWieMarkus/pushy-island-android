package markus.wieland.pushygame;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;
import markus.wieland.pushygame.levelbuilder.LevelBuilderItemAdapter;
import markus.wieland.pushygame.ui.PushyGridAdapter;
import markus.wieland.pushygame.ui.PushyView;

public class LevelBuilderActivity extends DefaultActivity implements OnItemClickListener<Type> {

    private String levelCode;

    private PushyView terrainView;
    private PushyView entityView;

    private LevelBuilder levelBuilder;

    private RecyclerView recyclerViewTerrain;
    private RecyclerView recyclerViewEntity;

    private LevelBuilderItemAdapter levelBuilderEntityAdapter;
    private LevelBuilderItemAdapter levelBuilderTerrainAdapter;

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

        findViewById(R.id.activity_level_builder_undo).setOnClickListener(view -> levelBuilder.undo());
        findViewById(R.id.actvity_level_builder_redo).setOnClickListener(view -> levelBuilder.redo());
        findViewById(R.id.activity_game_export).setOnClickListener(view -> levelCode = levelBuilder.export());
        findViewById(R.id.activity_level_builder_fill).setOnClickListener(view -> levelBuilder.fill());
        findViewById(R.id.activity_level_builder_smooth).setOnClickListener(view -> levelBuilder.smooth());
        findViewById(R.id.activity_level_builder_import).setOnClickListener(view -> levelBuilder.importLevel(levelCode));

        recyclerViewTerrain.setHasFixedSize(true);
        recyclerViewEntity.setHasFixedSize(true);
        recyclerViewTerrain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEntity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }

    @Override
    public void execute() {
        levelBuilder = new LevelBuilder(this);

        levelBuilderEntityAdapter = new LevelBuilderItemAdapter(this);
        levelBuilderTerrainAdapter = new LevelBuilderItemAdapter(this);

        levelBuilderTerrainAdapter.select(levelBuilder.getSelectedField());

        levelBuilderEntityAdapter.submitList(EntityType.class.getEnumConstants());
        levelBuilderTerrainAdapter.submitList(TerrainType.class.getEnumConstants());

        recyclerViewTerrain.setAdapter(levelBuilderTerrainAdapter);
        recyclerViewEntity.setAdapter(levelBuilderEntityAdapter);

        terrainView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyTerrainViews()));
        entityView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyEntityViews()));


    }

    public void onClick(Type type) {
        levelBuilder.setSelectedField(type);
        levelBuilderTerrainAdapter.select(type);
        levelBuilderEntityAdapter.select(type);
    }

}