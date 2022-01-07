package markus.wieland.pushygame.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import markus.wieland.pushygame.engine.level.LevelDisplayItem;

@Database(entities = LevelDisplayItem.class, version = 2)
public abstract class LevelDatabase extends RoomDatabase {

    public abstract LevelDataAccessObject getLevelDataAccessObject();

    private static LevelDatabase instance;

    public static synchronized LevelDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LevelDatabase.class, "school_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }


}
