package markus.wieland.pushygame.ui.customlevel.levelbuilder;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.RawLevel;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;
import markus.wieland.pushygame.persistence.LevelViewModel;
import markus.wieland.pushygame.ui.game.PushyGridAdapter;
import markus.wieland.pushygame.ui.game.PushyView;

public class LevelBuilderActivity extends DefaultActivity implements OnItemClickListener<Type>, Observer<LevelDisplayItem> {

    public static final String KEY_LEVEL_ID = "markus.wieland.pushygame.ui.customlevel.levelbuilder.LEVEL_ID";
    public static final long NO_LEVEL_ID = -1;

    private PushyView terrainView;
    private PushyView entityView;

    private LevelBuilder levelBuilder;

    private RecyclerView recyclerViewTerrain;
    private RecyclerView recyclerViewEntity;

    private LevelBuilderItemAdapter levelBuilderEntityAdapter;
    private LevelBuilderItemAdapter levelBuilderTerrainAdapter;

    private LevelViewModel levelViewModel;

    private long levelId;

    private LevelDisplayItem levelDisplayItem;

    public LevelBuilderActivity() {
        super(R.layout.activity_level_builder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void bindViews() {
        levelViewModel = ViewModelProviders.of(this).get(LevelViewModel.class);
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
        findViewById(R.id.activity_game_export).setOnClickListener(this::export);
        findViewById(R.id.activity_level_builder_fill).setOnClickListener(view -> {
            levelBuilder.fill();
            ((ImageButton)findViewById(R.id.activity_level_builder_fill)).setImageResource(levelBuilder.isFillMode() ? R.drawable.ic_edit : R.drawable.ic_fill);
        });
        findViewById(R.id.activity_level_builder_smooth).setOnClickListener(view -> levelBuilder.smooth());

        recyclerViewTerrain.setHasFixedSize(true);
        recyclerViewEntity.setHasFixedSize(true);
        recyclerViewTerrain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEntity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }

    public void export(View view) {
        if (!levelBuilder.hasChanges()) {
            finish();
            return;
        }
        if (!levelBuilder.validate()) {
            return;
        }
        String levelCode = levelBuilder.export();
        if (levelId != NO_LEVEL_ID) {
            levelDisplayItem.setFile(levelCode);
            levelDisplayItem.setThumbnail(LevelBuilder.createThumbnail(this, levelBuilder.getTerrainManager(), levelBuilder.getEntityManager()));
            levelViewModel.update(levelDisplayItem);
            finish();
            return;
        }
        levelViewModel.insert(new LevelDisplayItem(new RawLevel(levelCode), levelCode, LevelBuilder.createThumbnail(this, levelBuilder.getTerrainManager(), levelBuilder.getEntityManager())));
        finish();
    }

    @Override
    public void execute() {

        levelId = getIntent().getLongExtra(KEY_LEVEL_ID, NO_LEVEL_ID);

        levelBuilder = new LevelBuilder(this);
        if (levelId != NO_LEVEL_ID) {
            levelViewModel.getLevel(levelId).observe(this, this);
        }

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


    @Override
    public void onChanged(LevelDisplayItem levelDisplayItem) {
        this.levelDisplayItem = levelDisplayItem;
        levelBuilder.importLevel(levelDisplayItem.getFile());
    }
}