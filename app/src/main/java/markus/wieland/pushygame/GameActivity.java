package markus.wieland.pushygame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.GameEventListener;
import markus.wieland.pushygame.engine.events.InventoryEventListener;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Inventory;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.Level;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.LevelLoader;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.persistence.LevelViewModel;
import markus.wieland.pushygame.ui.InventoryAdapter;
import markus.wieland.pushygame.ui.InventoryItem;
import markus.wieland.pushygame.ui.PushyFieldView;
import markus.wieland.pushygame.ui.PushyGridAdapter;
import markus.wieland.pushygame.ui.PushyView;

public class GameActivity extends DefaultActivity implements GameEventListener, InventoryEventListener {

    public static final String LEVEL_PATH = "markus.wieland.pushy.LEVEL_PATH";

    private PushyView terrain;
    private PushyView entities;
    private Game gameManager;

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
        up.setOnClickListener(view -> gameManager.move(Direction.NORTH));
        down.setOnClickListener(view -> gameManager.move(Direction.SOUTH));
        left.setOnClickListener(view -> gameManager.move(Direction.WEST));
        right.setOnClickListener(view -> gameManager.move(Direction.EAST));
        restart.setOnClickListener(view -> recreate());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void execute() {
        inventoryAdapter = new InventoryAdapter();
        recyclerView.setAdapter(inventoryAdapter);
        String path = getIntent().getStringExtra(LEVEL_PATH);
        levelDisplayItem = new LevelDisplayItem(path);
        levelNumber.setText(levelDisplayItem.getNumber());
        levelName.setText(levelDisplayItem.getName());
        Level level = LevelLoader.buildLevel(this, path);

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
        gameManager = new Game(pushyEntityViews, pushyTerrainViews);
        gameManager.setGameEventListener(this);
        gameManager.setInventoryEventListener(this);


    }

    @Override
    public void onFinish() {
        levelDisplayItem.setSolved(true);
        levelViewModel.update(levelDisplayItem);
        String nextLevel = LevelLoader.getNextLevel(this, getIntent().getStringExtra(LEVEL_PATH));
        if (nextLevel != null) {
            startActivity(new Intent(this, GameActivity.class).putExtra(LEVEL_PATH, nextLevel));
        }
        finish();
    }

    @Override
    public void onInventoryChanged() {
       inventoryAdapter.submitList(gameManager.getInventory().getInventoryItems());
    }
}