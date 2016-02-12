package io.kodokojo.project;

import io.kodokojo.project.model.Service;

public interface ServiceHealthChecker {

    ServiceHealth checkServiceState(Service service);

}
