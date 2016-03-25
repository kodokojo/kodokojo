package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.BrickConfiguration;

import java.io.Serializable;

public class BrickConfigDto implements Serializable {

    private String name;

    private String type;

    public BrickConfigDto(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public BrickConfigDto(BrickConfiguration brickConfiguration) {
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        this.name = brickConfiguration.getName();
        this.type = brickConfiguration.getType().name();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BrickConfigDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
