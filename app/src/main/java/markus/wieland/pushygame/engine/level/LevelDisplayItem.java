package markus.wieland.pushygame.engine.level;

import markus.wieland.defaultappelements.uielements.adapter.QueryableEntity;

public class LevelDisplayItem implements QueryableEntity<String> {

    private String name;
    private String file;
    private String number;

    public LevelDisplayItem(String file) {
        String[] parts = file.split("_");
        this.name = parts[1].replace(".json", "");
        ;
        this.number = parts[0];
        this.file = file;
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
}