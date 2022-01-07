package markus.wieland.pushygame.persistence;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import markus.wieland.databases.BaseViewModel;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;

public class LevelViewModel extends BaseViewModel<LevelDisplayItem, LevelDataAccessObject, LevelRepository> {

    public LevelViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected LevelRepository initRepository() {
        return new LevelRepository(getApplication());
    }

    public LiveData<List<LevelDisplayItem>> getAllLevel() {
        return getRepository().getAllLevel();
    }
}
