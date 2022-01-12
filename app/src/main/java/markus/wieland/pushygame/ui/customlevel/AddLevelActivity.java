package markus.wieland.pushygame.ui.customlevel;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import markus.wieland.defaultappelements.uielements.activities.DefaultActivity;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;
import markus.wieland.pushygame.engine.level.RawLevel;
import markus.wieland.pushygame.persistence.LevelViewModel;

public class AddLevelActivity extends DefaultActivity implements View.OnClickListener {

    private EditText codeInput;
    private LevelViewModel levelViewModel;
    private Button save;

    public AddLevelActivity() {
        super(R.layout.activity_add_level);
    }

    @Override
    public void bindViews() {
        codeInput = findViewById(R.id.activity_add_level_text_input);
        levelViewModel = ViewModelProviders.of(this).get(LevelViewModel.class);
        save = findViewById(R.id.activity_add_level_save);
    }

    @Override
    public void initializeViews() {
        save.setOnClickListener(this);
    }

    @Override
    public void execute() {
        // TODO add functionality to import a level
    }

    private RawLevel validate(String code) {

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter a level code", Toast.LENGTH_SHORT).show();
            return null;
        }

        try {
            return new RawLevel(code);
        } catch (Exception exception) {
            Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        String code = codeInput.getText().toString().trim();
        RawLevel rawLevel = validate(code);
        if (rawLevel != null) {
            LevelDisplayItem levelDisplayItem = new LevelDisplayItem(rawLevel, code);
            levelViewModel.insert(levelDisplayItem);
        }
    }
}