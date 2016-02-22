package io.kodokojo.project.launcher;

import io.kodokojo.commons.project.model.Configuration;

public interface ConfigurationApplierFactory<C extends Configuration,L> {

    ConfigurationApplier<C,L> create();

}
