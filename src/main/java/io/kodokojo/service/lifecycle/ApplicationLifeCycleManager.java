package io.kodokojo.service.lifecycle;

import java.util.HashSet;
import java.util.Set;

public class ApplicationLifeCycleManager {

    private Set<ApplicationLifeCycleListener> listeners = new HashSet<>();

    public void addService(ApplicationLifeCycleListener applicationLifeCycleListener) {
        listeners.add(applicationLifeCycleListener);
    }

    public void stop() {
        listeners.forEach(ApplicationLifeCycleListener::stop);
    }

}
