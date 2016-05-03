package io.kodokojo.brick.dockerregistry;

import io.kodokojo.brick.BrickConfigurationException;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.model.User;

import java.util.List;

public class DockerRegistryConfigurer implements BrickConfigurer {

    @Override
    public BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) throws BrickConfigurationException {
        return brickConfigurerData;
    }

    @Override
    public BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException {
        return brickConfigurerData;
    }

}
