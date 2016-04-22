package io.kodokojo.service;

import io.kodokojo.model.StackConfiguration;

/**
 * {@link StackConfiguration} repository.
 */
public interface StackConfigurationStore {

    void save(String projectName, StackConfiguration stackConfiguration);

    StackConfiguration getStackConfiguration(String projectName, String stackName);

}
