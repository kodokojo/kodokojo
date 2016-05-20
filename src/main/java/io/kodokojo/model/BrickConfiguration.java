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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickConfiguration implements Configuration, Serializable {

    private final Brick brick;

    private final String name;

    private final BrickType type;

    private final String url;

    private String version;

    private Date versionDate;

    private final boolean waitRunning;

    private Map<String, Serializable> customData;

    public BrickConfiguration(Brick brick, String name, BrickType type, String url, boolean waitRunning) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
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
        this.waitRunning = waitRunning;
        this.customData = new HashMap<>();
    }
    public BrickConfiguration(Brick brick, String name, BrickType type, String url) {
        this(brick, name, type, url, true);
    }

    public BrickConfiguration(String name, BrickType type, String url) {
        this(null, name, type, url);
    }

    public BrickConfiguration(Brick brick, boolean waitRunning) {
        this(brick, brick.getName(), brick.getType(), null, waitRunning);
    }
    public BrickConfiguration(Brick brick) {
        this(brick, brick.getName(), brick.getType(), null);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Date getVersionDate() {
        return versionDate;
    }

    @Override
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
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
        return versionDate != null ? versionDate.equals(that.versionDate) : that.versionDate == null;

    }

    @Override
    public int hashCode() {
        int result = brick != null ? brick.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (versionDate != null ? versionDate.hashCode() : 0);
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
                ", versionDate=" + versionDate +
                '}';
    }
}
