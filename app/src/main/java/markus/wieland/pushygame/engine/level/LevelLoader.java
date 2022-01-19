package markus.wieland.pushygame.engine.level;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LevelLoader {

    private static final Gson GSON = new Gson();

    private static final String LEVEL_PATH = "level";

    private LevelLoader() {
    }

    public static List<LevelDisplayItem> getLocalLevels(Activity activity) {
        String[] list;
        try {
            list = activity.getAssets().list(LEVEL_PATH);
        } catch (IOException e) {
            return new ArrayList<>();
        }

        List<LevelDisplayItem> levels = new ArrayList<>();
        for (String level : list) {
            levels.add(new LevelDisplayItem(level));
        }
        return levels;
    }

    public static String getNextLevel(Activity activity, int id) {
        List<LevelDisplayItem> levelDisplayItems = getLocalLevels(activity);
        if (id >= levelDisplayItems.size()) return null;
        // We don't have to increment because level number starts at index 1
        return getLocalLevels(activity).get(id).getFile();
    }

    private static RawLevel load(Activity activity, String name) {
        String json;
        try (InputStream inputStream = activity.getAssets().open(LEVEL_PATH + "/" + name)) {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            long bytes = inputStream.read(buffer);
            Log.i("LevelLoader", "Loaded file " + name + " size: " +bytes + " bytes");
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return GSON.fromJson(json, RawLevel.class);
    }

    public static Level buildLevel(Activity activity, String name) {
        return new Level(Objects.requireNonNull(load(activity, name)));
    }
}
