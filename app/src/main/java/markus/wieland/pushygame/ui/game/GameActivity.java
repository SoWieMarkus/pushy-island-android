package markus.wieland.pushygame.ui.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.GameEventListener;
import markus.wieland.pushygame.engine.events.InventoryEventListener;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.Level;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.LevelLoader;
import markus.wieland.pushygame.engine.level.RawLevel;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.persistence.LevelViewModel;

public class GameActivity extends DefaultActivity implements GameEventListener, InventoryEventListener, Observer<LevelDisplayItem> {

    public static final String LEVEL_PATH = "markus.wieland.pushy.LEVEL_PATH";
    public static final String LEVEL_ID = "markus.wieland.pushy.LEVEL_ID";
    public static final String LEVEL_CODE = "markus.wieland.pushy.LEVEL_CODE";

    private PushyView terrain;
    private PushyView entities;
    private Game game;

    private TextView levelName;
    private TextView levelNumber;
    private ImageButton restart;

    private ImageButton up;
    private ImageButton down;
    private ImageButton left;
    private ImageButton right;

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private LevelViewModel levelViewModel;
    private LevelDisplayItem levelDisplayItem;

    private long id;


    public GameActivity() {
        super(R.layout.activity_game);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void bindViews() {
        terrain = findViewById(R.id.game_activity_terrain);
        entities = findViewById(R.id.game_activity_entities);
        up = findViewById(R.id.activity_game_up);
        left = findViewById(R.id.activity_game_left);
        down = findViewById(R.id.activity_game_down);
        right = findViewById(R.id.activity_game_right);
        levelName = findViewById(R.id.activity_game_level_name);
        levelNumber = findViewById(R.id.activity_game_level_number);
        restart = findViewById(R.id.activity_game_restart);
        recyclerView = findViewById(R.id.activity_game_recycler_view);
        levelViewModel = ViewModelProviders.of(this).get(LevelViewModel.class);
    }

    @Override
    public void initializeViews() {
        up.setOnClickListener(view -> game.move(Direction.NORTH));
        down.setOnClickListener(view -> game.move(Direction.SOUTH));
        left.setOnClickListener(view -> game.move(Direction.WEST));
        right.setOnClickListener(view -> game.move(Direction.EAST));
        restart.setOnClickListener(view -> recreate());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void execute() {
        inventoryAdapter = new InventoryAdapter();
        recyclerView.setAdapter(inventoryAdapter);
        id = getIntent().getLongExtra(LEVEL_ID, 1);
        levelViewModel.getLevel(id).observe(this, this);
    }


    @Override
    public void onFinish() {

        levelDisplayItem.setSolved(true);
        levelViewModel.update(levelDisplayItem);
        if (getIntent().getStringExtra(LEVEL_PATH) == null) {
            finish();
            return;
        }

        String nextLevel = LevelLoader.getNextLevel(this, (int) id);
        if (nextLevel != null) {
            startActivity(new Intent(this, GameActivity.class).putExtra(LEVEL_PATH, nextLevel));
        }
        finish();
    }

    @Override
    public void onInventoryChanged() {
        inventoryAdapter.submitList(game.getInventory().getInventoryItems());
    }

    @Override
    public void onChanged(LevelDisplayItem levelDisplayItem) {
        this.levelDisplayItem = levelDisplayItem;

        String path = getIntent().getStringExtra(LEVEL_PATH);
        String code = getIntent().getStringExtra(LEVEL_CODE);
        Level level;
        if (path != null) {
            levelNumber.setText(levelDisplayItem.getNumberAsString());
            levelName.setText(levelDisplayItem.getName());
            level = LevelLoader.buildLevel(this, path);
        } else {
            RawLevel rawLevel = new RawLevel(code);
            level = new Level(rawLevel);
        }

        terrain.setNumColumns(level.getTerrain().getSizeY());
        entities.setNumColumns(level.getTerrain().getSizeY());

        Matrix<PushyFieldView<Terrain>> pushyTerrainViews = new Matrix<>(level.getTerrain().getSizeX(), level.getTerrain().getSizeY());
        Matrix<PushyFieldView<Entity>> pushyEntityViews = new Matrix<>(level.getEntities().getSizeX(), level.getEntities().getSizeY());

        for (int x = 0; x < level.getHeight(); x++) {
            for (int y = 0; y < level.getWidth(); y++) {
                PushyFieldView<Terrain> pushyTerrainView = new PushyFieldView<>(this, level.getTerrain().get(x, y));
                pushyTerrainViews.set(x, y, pushyTerrainView);
                PushyFieldView<Entity> pushyEntityView = new PushyFieldView<>(this, level.getEntities().get(x, y));
                pushyEntityViews.set(x, y, pushyEntityView);
                pushyEntityView.invalidate();
                pushyTerrainView.invalidate();
            }
        }

        terrain.setAdapter(new PushyGridAdapter<>(pushyTerrainViews));
        entities.setAdapter(new PushyGridAdapter<>(pushyEntityViews));
        game = new Game(this, pushyEntityViews, pushyTerrainViews);
        game.setGameEventListener(this);
        game.setInventoryEventListener(this);
    }
}