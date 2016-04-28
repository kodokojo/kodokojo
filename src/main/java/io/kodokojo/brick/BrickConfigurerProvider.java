package io.kodokojo.brick;

import io.kodokojo.model.Brick;
import io.kodokojo.brick.BrickConfigurer;

public interface BrickConfigurerProvider {

    BrickConfigurer provideFromBrick(Brick brick);

}
