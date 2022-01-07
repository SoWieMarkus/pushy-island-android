package markus.wieland.pushygame.engine.exceptions;

import markus.wieland.pushygame.engine.level.Tag;

public class UnknownTileException extends RuntimeException{

    public UnknownTileException(Tag tag){
        super("This tile is unknown: " + tag);
    }
}
