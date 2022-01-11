package markus.wieland.pushygame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.ui.campaign.CampaignActivity;
import markus.wieland.pushygame.ui.customlevel.CustomLevelActivity;
import markus.wieland.pushygame.ui.customlevel.levelbuilder.LevelBuilderActivity;

public class MainActivity extends DefaultActivity {

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void bindViews() {

    }

    @Override
    public void initializeViews() {
        findViewById(R.id.activity_main_campaign).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CampaignActivity.class)));
        findViewById(R.id.activity_main_level_builder).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CustomLevelActivity.class)));
    }

    @Override
    public void execute() {

    }
}
