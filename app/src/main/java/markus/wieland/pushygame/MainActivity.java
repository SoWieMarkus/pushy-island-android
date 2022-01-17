package markus.wieland.pushygame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.LevelLoader;
import markus.wieland.pushygame.persistence.LevelViewModel;
import markus.wieland.pushygame.ui.campaign.CampaignActivity;
import markus.wieland.pushygame.ui.customlevel.CustomLevelActivity;

public class MainActivity extends DefaultActivity implements Observer<List<LevelDisplayItem>> {

    private LevelViewModel levelViewModel;
    private final List<LevelDisplayItem> levelDisplayItems;

    public MainActivity() {
        super(R.layout.activity_main);
        this.levelDisplayItems = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void bindViews() {
        levelViewModel = ViewModelProviders.of(this).get(LevelViewModel.class);
    }

    @Override
    public void initializeViews() {
        findViewById(R.id.activity_main_campaign).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CampaignActivity.class)));
        findViewById(R.id.activity_main_level_builder).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CustomLevelActivity.class)));
        findViewById(R.id.cheat).setOnClickListener(view -> {
            for (LevelDisplayItem levelDisplayItem : levelDisplayItems) {
                if (!levelDisplayItem.isSolved()) {
                    levelDisplayItem.setSolved(true);
                    levelViewModel.update(levelDisplayItem);
                }
            }
        });
    }

    @Override
    public void execute() {
        levelViewModel.getAllLevel(true).observe(this, this);
    }

    @Override
    public void onChanged(List<LevelDisplayItem> levelDisplayItems) {
        this.levelDisplayItems.clear();
        this.levelDisplayItems.addAll(levelDisplayItems);
        if (levelDisplayItems.isEmpty()) {
            for (LevelDisplayItem levelDisplayItem : LevelLoader.getLocalLevels(this)) {
                levelViewModel.insert(levelDisplayItem);
            }
            return;
        }

        findViewById(R.id.activity_main_level_builder).setEnabled(levelDisplayItems.get(0).isSolved());
    }
}
