package markus.wieland.pushygame.ui.customlevel;

import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemInteractListener;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;

public interface CustomLevelInteractionListener extends OnItemInteractListener<LevelDisplayItem> {

    void onEdit(LevelDisplayItem levelDisplayItem);
    void onShare(LevelDisplayItem levelDisplayItem);
    void onPlay(LevelDisplayItem levelDisplayItem);
    void onDelete(LevelDisplayItem levelDisplayItem);

}
