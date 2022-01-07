package markus.wieland.pushygame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.LevelLoader;
import markus.wieland.pushygame.ui.PushyLevelAdapter;

public class CampaignActivity extends DefaultActivity implements OnItemClickListener<LevelDisplayItem> {

    private RecyclerView recyclerView;

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
    }

    @Override
    public void execute() {
        PushyLevelAdapter pushyLevelAdapter = new PushyLevelAdapter(this);
        pushyLevelAdapter.submitList(LevelLoader.getLocalLevels(this));
        recyclerView.setAdapter(pushyLevelAdapter);
    }

    @Override
    public void onClick(LevelDisplayItem levelDisplayItem) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.LEVEL_PATH, levelDisplayItem.getFile());
        startActivity(intent);
    }
}