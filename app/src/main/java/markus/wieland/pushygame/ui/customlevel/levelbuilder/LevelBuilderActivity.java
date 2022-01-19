package markus.wieland.pushygame.ui.customlevel.levelbuilder;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.textinputvalidator.TextInputValidator;
import markus.wieland.defaultappelements.textinputvalidator.ValidatorResult;
import markus.wieland.defaultappelements.textinputvalidator.arguments.MaxLengthValidatorArgument;
import markus.wieland.defaultappelements.textinputvalidator.arguments.MinLengthValidatorArgument;
import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.LevelConverter;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.RawLevel;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;
import markus.wieland.pushygame.levelbuilder.SetMode;
import markus.wieland.pushygame.persistence.LevelViewModel;
import markus.wieland.pushygame.ui.dialog.Dialog;
import markus.wieland.pushygame.ui.dialog.DialogInteractionListener;
import markus.wieland.pushygame.ui.dialog.TextInputDialog;
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

    private TextInputDialog textInputDialog;

    private TextInputValidator textInputValidator;

    private ImageButton levelBuilderFillMode;
    private ImageButton levelBuilderPencilMode;
    private ImageButton levelBuilderEraseMode;
    private ImageButton levelBuilderUndo;
    private ImageButton levelBuilderRedo;
    private ImageButton levelBuilderSmooth;
    private ImageButton levelBuilderSave;
    private ImageButton levelBuilderName;
    private ImageButton levelBuilderReset;

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

        levelBuilderName = findViewById(R.id.activity_level_builder_name);
        levelBuilderPencilMode = findViewById(R.id.activity_level_builder_pencil);
        levelBuilderEraseMode = findViewById(R.id.activity_level_builder_erase);
        levelBuilderUndo = findViewById(R.id.activity_level_builder_undo);
        levelBuilderRedo = findViewById(R.id.actvity_level_builder_redo);
        levelBuilderSmooth = findViewById(R.id.activity_level_builder_smooth);
        levelBuilderSave = findViewById(R.id.activity_level_builder_export);
        levelBuilderReset = findViewById(R.id.activity_level_builder_reset);
        levelBuilderFillMode = findViewById(R.id.activity_level_builder_fill);
    }

    @Override
    public void initializeViews() {
        terrainView.setNumColumns(LevelBuilder.LEVEL_WIDTH);
        entityView.setNumColumns(LevelBuilder.LEVEL_WIDTH);

        levelBuilderName.setVisibility(View.GONE);
        levelBuilderName.setOnClickListener(view -> textInputDialog.getDialog().show());

        levelBuilderUndo.setOnClickListener(view -> levelBuilder.undo());
        levelBuilderRedo.setOnClickListener(view -> levelBuilder.redo());
        levelBuilderReset.setOnClickListener(view -> levelBuilder.reset());
        levelBuilderSave.setOnClickListener(this::export);
        levelBuilderFillMode.setOnClickListener(view -> select(SetMode.FILL));
        levelBuilderEraseMode.setOnClickListener(view -> select(SetMode.ERASE));
        levelBuilderPencilMode.setOnClickListener(view -> select(SetMode.PENCIL));
        levelBuilderSmooth.setOnClickListener(view -> levelBuilder.smooth());

        recyclerViewTerrain.setHasFixedSize(true);
        recyclerViewEntity.setHasFixedSize(true);
        recyclerViewTerrain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEntity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        textInputDialog = new TextInputDialog(this, null);
        textInputDialog.setMessage(getString(R.string.level_builder_enter_name))
                .setOkMessage(getString(R.string.dialog_commit))
                .setDeclineMessage(getString(R.string.dialog_cancel));
    }

    public void select(SetMode setMode) {

        int highlightColor = Color.rgb(0,50,255);

        levelBuilderPencilMode.setColorFilter(Color.WHITE);
        levelBuilderEraseMode.setColorFilter(Color.WHITE);
        levelBuilderFillMode.setColorFilter(Color.WHITE);
        levelBuilder.setSetMode(setMode);
        switch (setMode){
            case ERASE:
                levelBuilderEraseMode.setColorFilter(highlightColor);
                break;
            case FILL:
                levelBuilderFillMode.setColorFilter(highlightColor);
                break;
            default:
                levelBuilderPencilMode.setColorFilter(highlightColor);
                break;

        }
    }

    public void export(View view) {
        if (levelBuilder.hasNoChanges()) {
            finish();
            return;
        }

        if (!levelBuilder.validate()) return;

        String levelCode = levelBuilder.export("Levelname");
        if (levelId != NO_LEVEL_ID) {
            levelDisplayItem.setFile(levelCode);
            levelDisplayItem.setSolved(false);
            levelDisplayItem.setThumbnail(LevelConverter.createThumbnail(this, levelBuilder.getTerrainManager(), levelBuilder.getEntityManager()));
            levelViewModel.update(levelDisplayItem);
            finish();
            return;
        }
        levelViewModel.insert(new LevelDisplayItem(new RawLevel(levelCode), levelCode, LevelConverter.createThumbnail(this, levelBuilder.getTerrainManager(), levelBuilder.getEntityManager())));
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

        levelBuilderEntityAdapter.submitList(Type.getListByFirstAppearance(EntityType.class.getEnumConstants(), 177));
        levelBuilderTerrainAdapter.submitList(Type.getListByFirstAppearance(TerrainType.class.getEnumConstants(), 177));

        recyclerViewTerrain.setAdapter(levelBuilderTerrainAdapter);
        recyclerViewEntity.setAdapter(levelBuilderEntityAdapter);

        terrainView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyTerrainViews()));
        entityView.setAdapter(new PushyGridAdapter<>(levelBuilder.getPushyEntityViews()));

        textInputValidator = new TextInputValidator();
        textInputValidator.add(new MinLengthValidatorArgument(1, getString(R.string.error_level_builder_name_too_short)));
        textInputValidator.add(new MaxLengthValidatorArgument(20, getString(R.string.error_level_builder_name_too_long)));

    }

    public void onClick(Type type) {
        levelBuilder.setSelectedField(type);
        levelBuilderTerrainAdapter.select(type);
        levelBuilderEntityAdapter.select(type);
    }

    @Override
    public void onBackPressed() {
        if (levelBuilder.isSaved()) {
            super.onBackPressed();
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.setMessage(getString(R.string.dialog_changes_without_save))
                .setOkEvent(alertDialog -> LevelBuilderActivity.super.onBackPressed())
                .getDialog()
                .show();
    }

    @Override
    public void onChanged(LevelDisplayItem levelDisplayItem) {
        this.levelDisplayItem = levelDisplayItem;
        textInputDialog.setText(levelDisplayItem.getName());
        levelBuilderName.setVisibility(View.VISIBLE);
        levelBuilder.importLevel(levelDisplayItem.getFile());
    }
}