package io.kodokojo.service.repository.store;

import io.kodokojo.model.Entity;

public interface EntityStore {

    EntityStoreModel getEntityById(String entityIdentifier);

    String getEntityIdOfUserId(String userIdentifier);

    String addEntity(EntityStoreModel entity);

    void addUserToEntity(String userIdentifier, String entityIdentifier);
}
