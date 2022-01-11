package markus.wieland.pushygame.engine.level;

import androidx.room.PrimaryKey;

import markus.wieland.databases.DatabaseEntity;
import markus.wieland.defaultappelements.uielements.adapter.QueryableEntity;

@androidx.room.Entity(tableName = "level")
public class LevelDisplayItem implements QueryableEntity<Long>, DatabaseEntity {

    private String name;
    private String file;

    @PrimaryKey(autoGenerate = true)
    private long number;
    private boolean solved;

    private boolean isCampaign;

    public LevelDisplayItem(String file) {
        String[] parts = file.split("_");
        this.name = parts[1].replace(".json", "");
        this.number = Long.parseLong(parts[0]);
        this.file = file;
        this.solved = false;
        this.isCampaign = true;
    }

    public LevelDisplayItem(RawLevel rawLevel, String code) {
        this.name = rawLevel.getName();
        this.file = code;
        this.solved = false;
        this.isCampaign = false;
    }

    public LevelDisplayItem() {
    }

    public boolean isCampaign() {
        return isCampaign;
    }

    public void setCampaign(boolean campaign) {
        isCampaign = campaign;
    }

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

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getNumberAsString() {
        StringBuilder number = new StringBuilder(String.valueOf(getNumber()));
        while (number.length() < 3) {
            number.insert(0, "0");
        }
        return number.toString();
    }

    @Override
    public Long getId() {
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
        return getNumber();
    }
}