package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.StackConfiguration;

import java.util.ArrayList;
import java.util.List;

public class StackConfigDto {

    private String name;

    private String type;

    private List<BrickConfigDto> brickConfigs;

    public StackConfigDto(String name, String type, List<BrickConfigDto> brickConfigs) {
        this.name = name;
        this.type = type;
        this.brickConfigs = brickConfigs;
    }

    public StackConfigDto(StackConfiguration stackConfiguration) {
        if (stackConfiguration == null) {
            throw new IllegalArgumentException("stackConfiguration must be defined.");
        }
        this.name = stackConfiguration.getName();
        this.type = stackConfiguration.getType().name();
        this.brickConfigs = new ArrayList<>(stackConfiguration.getBrickConfigurations().size());
        stackConfiguration.getBrickConfigurations().forEach(brickConfiguration -> brickConfigs.add(new BrickConfigDto(brickConfiguration)));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<BrickConfigDto> getBrickConfigs() {
        return brickConfigs;
    }

    public void setBrickConfigs(List<BrickConfigDto> brickConfigs) {
        this.brickConfigs = brickConfigs;
    }

    @Override
    public String toString() {
        return "StackConfigDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", brickConfigs=" + brickConfigs +
                '}';
    }
}
