package io.kodokojo.brick;

import io.kodokojo.model.Brick;

public interface BrickFactory {

    Brick createBrick(String name);

}
