package io.kodokojo.service.store;

import io.kodokojo.model.Entity;

public interface EntityFetcher {

    Entity getEntityById(String entityIdentifier);

    String getEntityIdOfUserId(String userIdentifier);
}
