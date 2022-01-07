package markus.wieland.pushygame.engine.level;

import android.app.Activity;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LevelLoader {
    private static final Gson GSON = new Gson();

    private LevelLoader() {
    }

    public static List<LevelDisplayItem> getLocalLevels(Activity activity){
        String[] list;
        try {
            list = activity.getAssets().list("level");
        } catch (IOException e) {
            return new ArrayList<>();
        }

        List<LevelDisplayItem> levels = new ArrayList<>();
        for (String level : list) {
            levels.add(new LevelDisplayItem(level));
        }
        return levels;
    }

    public static String getNextLevel(Activity activity, String levelName){
        LevelDisplayItem levelDisplayItem = new LevelDisplayItem(levelName);
        int levelNumber = Integer.parseInt(levelDisplayItem.getNumber()); // We dont have to increment because level number starts at index 1
        List<LevelDisplayItem> levelDisplayItems = getLocalLevels(activity);
        if (levelNumber >= levelDisplayItems.size()) return null;
        return getLocalLevels(activity).get(levelNumber).getFile();
    }

    private static RawLevel load(Activity activity, String name) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open("level/" + name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return GSON.fromJson(json, RawLevel.class);
    }

    public static Level buildLevel(Activity activity, String name){
        return new Level(Objects.requireNonNull(load(activity, name)));
    }
}
