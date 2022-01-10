package markus.wieland.pushygame;

import android.content.Intent;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;

public class MainActivity extends DefaultActivity {

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    public void bindViews() {

    }

    @Override
    public void initializeViews() {
        findViewById(R.id.activity_main_campaign).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CampaignActivity.class)));
        findViewById(R.id.activity_main_level_builder).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LevelBuilderActivity.class)));
    }

    @Override
    public void execute() {

    }
}
