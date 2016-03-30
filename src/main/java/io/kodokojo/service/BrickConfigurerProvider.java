package io.kodokojo.service;

import io.kodokojo.model.Brick;
import io.kodokojo.project.starter.BrickConfigurer;

public interface BrickConfigurerProvider {

    BrickConfigurer provideFromBrick(Brick brick);

}
