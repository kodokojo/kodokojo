package io.kodokojo.service.repository.store;

public interface EntityStore {

    EntityStoreModel getEntityById(String entityIdentifier);

    String getEntityIdOfUserId(String userIdentifier);

    String addEntity(EntityStoreModel entity);

    void addUserToEntity(String userIdentifier, String entityIdentifier);
}
