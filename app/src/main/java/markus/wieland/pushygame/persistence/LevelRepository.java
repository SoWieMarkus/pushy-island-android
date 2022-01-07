package markus.wieland.pushygame.persistence;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import markus.wieland.databases.BaseDataAccessObject;
import markus.wieland.databases.BaseRepository;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;

public class LevelRepository extends BaseRepository<LevelDisplayItem, LevelDataAccessObject> {

    public LevelRepository(@NonNull Application application) {
        super(application);
    }

    @Override
    public LevelDataAccessObject initDataAccessObject(@NonNull Application application) {
        return LevelDatabase.getInstance(application).getLevelDataAccessObject();
    }

    public LiveData<List<LevelDisplayItem>> getAllLevel() {
        return getDataAccessObject().getAllLevel();
    }
}
