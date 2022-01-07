package markus.wieland.pushygame.engine.level;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import markus.wieland.databases.DatabaseEntity;
import markus.wieland.defaultappelements.uielements.adapter.QueryableEntity;

@androidx.room.Entity(tableName = "level")
public class LevelDisplayItem implements QueryableEntity<String>, DatabaseEntity {

    private String name;
    private String file;

    @PrimaryKey
    @NonNull
    private String number;
    private boolean solved;

    public LevelDisplayItem(String file) {
        String[] parts = file.split("_");
        this.name = parts[1].replace(".json", "");
        this.number = parts[0];
        this.file = file;
        this.solved = false;
    }

    public LevelDisplayItem() {}

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String getId() {
        return getNumber();
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String getStringToApplyQuery() {
        return getFile();
    }

    @Override
    public long getUniqueId() {
        return Integer.getInteger(number);
    }
}