package markus.wieland.pushygame.engine.exceptions;

import markus.wieland.pushygame.engine.level.Type;

public class UnknownTileException extends RuntimeException {

    public UnknownTileException(Type type) {
        super("This tile is unknown: " + type);
    }
}
