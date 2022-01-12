package markus.wieland.pushygame.engine.entity;

import markus.wieland.pushygame.engine.entity.movable.MovableEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Field;
import markus.wieland.pushygame.engine.level.EntityType;

public abstract class Entity extends Field {

    private boolean teleported;

    public Entity(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    public boolean isMovableEntity() {
        return this instanceof MovableEntity;
    }

    public boolean isInteractEntity() {
        return this instanceof InteractableEntity;
    }

    public boolean isCollectibleEntity() {
        return this instanceof CollectibleEntity;
    }

    public boolean isExplodable() {
        return false;
    }

    public boolean isTeleported() {
        return teleported;
    }

    public void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }

    @Override
    public EntityType getType() {
        return (EntityType) super.getType();
    }
}
