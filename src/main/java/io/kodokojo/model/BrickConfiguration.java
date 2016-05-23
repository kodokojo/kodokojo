/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.model;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickConfiguration implements Serializable {

    private final Brick brick;

    private final String name;

    private final BrickType type;

    private final String url;

    private final String version;

    private final boolean waitRunning;

    private Map<String, Serializable> customData;

    public BrickConfiguration(Brick brick, String name, BrickType type, String url, String version, boolean waitRunning) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(version)) {
            throw new IllegalArgumentException("version must be defined.");
        }
        if (brick == null) {
            if (isBlank(url)) {
                throw new IllegalArgumentException("url must be defined.");
            }
        }
        this.brick = brick;
        this.name = name;
        this.type = type;
        this.url = url;
        this.version = version;
        this.waitRunning = waitRunning;
        this.customData = new HashMap<>();
    }
    public BrickConfiguration(Brick brick, String name, BrickType type, String url, String version) {
        this(brick, name, type, url, version, true);
    }

    public BrickConfiguration(String name, BrickType type, String url, String version) {
        this(null, name, type, url, version);
    }

    public BrickConfiguration(Brick brick, boolean waitRunning, String version) {
        this(brick, brick.getName(), brick.getType(), null, version, waitRunning);
    }
    public BrickConfiguration(Brick brick) {
        this(brick, brick.getName(), brick.getType(), null, brick.getVersion());
    }

    public String getVersion() {
        return version;
    }

    public Map<String, Serializable> getCustomData() {
        return customData;
    }

    public void setCustomData(Map<String, Serializable> customData) {
        if (customData == null) {
            throw new IllegalArgumentException("customData must be defined.");
        }
        this.customData.putAll(customData);
    }

    public Brick getBrick() {
        return brick;
    }

    public String getName() {
        return name;
    }

    public BrickType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isWaitRunning() {
        return waitRunning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrickConfiguration that = (BrickConfiguration) o;

        if (brick != that.brick) return false;
        if (!name.equals(that.name)) return false;
        if (type != that.type) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = brick != null ? brick.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BrickConfiguration{" +
                "brick=" + brick +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", waitRunning='" + waitRunning + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
