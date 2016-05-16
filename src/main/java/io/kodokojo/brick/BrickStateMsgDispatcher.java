package io.kodokojo.brick;

import io.kodokojo.model.BrickState;

import java.util.HashSet;
import java.util.Set;

public class BrickStateMsgDispatcher implements BrickStateMsgListener {

    private final Set<BrickStateMsgListener> listeners;

    public BrickStateMsgDispatcher() {
        this.listeners = new HashSet<>();
    }

    public void addListener(BrickStateMsgListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        this.listeners.add(listener);
    }

    @Override
    public void receive(BrickState brickState) {
        listeners.forEach(listener -> listener.receive(brickState));
    }
}
