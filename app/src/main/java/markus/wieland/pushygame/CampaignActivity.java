package markus.wieland.pushygame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.LevelLoader;
import markus.wieland.pushygame.persistence.LevelViewModel;
import markus.wieland.pushygame.ui.PushyLevelAdapter;

public class CampaignActivity extends DefaultActivity implements OnItemClickListener<LevelDisplayItem>, Observer<List<LevelDisplayItem>> {

    private RecyclerView recyclerView;
    private PushyLevelAdapter pushyLevelAdapter;
    private LevelViewModel levelViewModel;

    public CampaignActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void bindViews() {
        recyclerView = findViewById(R.id.activity_campaign_recycler_view);
    }

    @Override
    public void initializeViews() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerView.setHasFixedSize(true);
        levelViewModel = ViewModelProviders.of(this).get(LevelViewModel.class);
    }

    @Override
    public void execute() {
        levelViewModel.getAllLevel().observe(this, this);
        pushyLevelAdapter = new PushyLevelAdapter(this);
        pushyLevelAdapter.submitList(new ArrayList<>());
        recyclerView.setAdapter(pushyLevelAdapter);
    }

    @Override
    public void onClick(LevelDisplayItem levelDisplayItem) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.LEVEL_PATH, levelDisplayItem.getFile());
        startActivity(intent);
    }

    @Override
    public void onChanged(List<LevelDisplayItem> levelDisplayItems) {
        if (levelDisplayItems.isEmpty()) {
            for (LevelDisplayItem levelDisplayItem : LevelLoader.getLocalLevels(this)) {
                levelViewModel.insert(levelDisplayItem);
            }
        }

        List<LevelDisplayItem> levels = new ArrayList<>();
        for (LevelDisplayItem level : levelDisplayItems) {
            levels.add(level);
            if (!level.isSolved()) break;
        }
        pushyLevelAdapter.submitList(levels);
    }
}