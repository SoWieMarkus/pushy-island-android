package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.events.BoatEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.Boat;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.Water;

public abstract class MovableEntity extends Entity {

    public MovableEntity(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    public boolean isPushableIntoWater(){
        return false;
    }

    public boolean canCollectEntities(){
        return false;
    }

    public boolean canMoveEntities(){
        return false;
    }

    public boolean canInteractWithEntities(){
        return false;
    }

    public boolean canSwimWithABoat(){
        return false;
    }

    public boolean isMovePossible(Coordinate nextCoordinate, Game game) {
        if (game.getEntityManager().isNotInsideField(nextCoordinate)) return false;

        Direction direction = getCoordinate().getDirection(nextCoordinate);

        Entity nextEntity = game.getEntityManager().getObject(nextCoordinate);
        Terrain terrain = game.getTerrainManager().getObject(this);
        Terrain nextTerrain = game.getTerrainManager().getObject(nextCoordinate);

        // Check if Pushy want's to swim on a boat
        // ATTENTION! We return false in this case because every other movement will be done by the BoatEvent
        if (terrain instanceof Boat && nextTerrain.getElevation() == Terrain.ELEVATION_WATER && canSwimWithABoat()) {
            game.execute(new BoatEvent(getCoordinate(), nextCoordinate));
            return false;
        }

        // Check if next field is water, if so can it be pushed into water?
        if (nextTerrain instanceof Water) return isPushableIntoWater();

        //Check if the next field is higher than the current one. If so the move is not possible
        if (nextTerrain.getElevation() > terrain.getElevation()) return false;

        // If the next entity is null nothing is blocking our way and because
        if (nextEntity == null) return true;

        // Because the elevation cant be higher anymore its less or equal to the current height
        // In Both cases we can collect items on the next field
        // ATTENTION! This also includes Water. So if you are on land, and for what ever reason there is a collectible
        // entity on this field you can also collect it
        // This is not a bug. It's a feature.
        if (nextEntity.isCollectibleEntity() && canCollectEntities()){
            ((CollectibleEntity) nextEntity).collect(game);
            return true;
        }

        // When we are at the same height ...
        if (terrain.hasSameElevationAs(nextTerrain)) {

            // We can move objects ...
            if (nextEntity.isMovableEntity() && canMoveEntities())
                return ((MovableEntity)nextEntity).move(nextCoordinate.getNextCoordinate(direction), game);

            // or interact with them
            if (nextEntity.isInteractEntity() && canInteractWithEntities()) {
                ((InteractableEntity)nextEntity).interact(direction, game);
                return false;
            }
         }

        return false;

    }

    protected void executeMove(Coordinate nextCoordinate, Game game) {
        game.getEntityManager().swapFields(getCoordinate(), nextCoordinate);
    }

    public boolean move(Coordinate nextCoordinate, Game game) {
        if (isMovePossible(nextCoordinate, game)){
            executeMove(nextCoordinate, game);
            return true;
        }
        return false;
    }

}
