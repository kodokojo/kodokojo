package io.kodokojo.brick;

import io.kodokojo.model.BrickState;

public interface BrickStateMsgListener {

    void receive(BrickState brickState);

}
