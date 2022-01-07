package markus.wieland.pushygame.engine.exceptions;

import markus.wieland.pushygame.engine.helper.Direction;

public class StringDirectionException extends RuntimeException {
    public StringDirectionException(Direction first, Direction second){
        super("Invalid State: First " + first + " second: " + second);
    }
}
