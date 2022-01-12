package markus.wieland.pushygame.engine.level;

import android.graphics.Bitmap;

import androidx.room.PrimaryKey;

import java.io.ByteArrayOutputStream;

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

    private byte[] thumbnail;

    public LevelDisplayItem(String file) {
        String[] parts = file.split("_");
        this.name = parts[1].replace(".json", "");
        this.number = Long.parseLong(parts[0]);
        this.file = file;
        this.solved = false;
        this.isCampaign = true;
        this.thumbnail = new byte[0];
    }

    public LevelDisplayItem(RawLevel rawLevel, String code, Bitmap bitmap) {
        this.name = rawLevel.getName();
        this.file = code;
        this.solved = false;
        this.isCampaign = false;

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
        this.thumbnail = blob.toByteArray();
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setThumbnail(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
        this.thumbnail = blob.toByteArray();
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
        StringBuilder numberAsString = new StringBuilder(String.valueOf(getNumber()));
        while (numberAsString.length() < 3) {
            numberAsString.insert(0, "0");
        }
        return numberAsString.toString();
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