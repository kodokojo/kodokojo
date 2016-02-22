package io.kodokojo.project.launcher;

import io.kodokojo.commons.project.model.Configuration;

public interface ConfigurationApplier<C extends Configuration,L> {

    L apply(C configuration);

}
