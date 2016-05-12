package io.kodokojo.service.store;

import io.kodokojo.model.Entity;

public interface EntityStore  {

    String addEntity(Entity entity);

    Entity getEntityById(String entityIdentifier);

    String getEntityIdOfUserId(String userIdentifier);

    void addUserToEntity(String userIdentifier, String entityIdentifier);

}
