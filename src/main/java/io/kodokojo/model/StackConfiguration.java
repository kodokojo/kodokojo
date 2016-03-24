package io.kodokojo.model;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class StackConfiguration implements Configuration, Serializable {

    private final String name;

    private final StackType type;

    private final Set<BrickConfiguration> brickConfigurations;

    private final String loadBalancerIp;

    private final int scmSshPort;

    private String version;

    private Date versionDate;

    public StackConfiguration(String name, StackType type, Set<BrickConfiguration> brickConfigurations, String loadBalancerIp, int scmSshPort) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (CollectionUtils.isEmpty(brickConfigurations)) {
            throw new IllegalArgumentException("brickConfigurations must be defined.");
        }

        for (BrickType expectedType : BrickType.values()) {
     //       checkIfConfigurationExist(expectedType, brickConfigurations);
        }
        this.name = name;
        this.type = type;
        this.brickConfigurations = brickConfigurations;
        this.loadBalancerIp = loadBalancerIp;
        this.scmSshPort = scmSshPort;
    }

    public String getName() {
        return name;
    }

    public StackType getType() {
        return type;
    }

    public Set<BrickConfiguration> getBrickConfigurations() {
        return new HashSet<>(brickConfigurations);
    }

    public String getLoadBalancerIp() {
        return loadBalancerIp;
    }

    public int getScmSshPort() {
        return scmSshPort;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackConfiguration stackConfiguration = (StackConfiguration) o;

        if (!name.equals(stackConfiguration.name)) return false;
        if (type != stackConfiguration.type) return false;
        return brickConfigurations.equals(stackConfiguration.brickConfigurations);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + brickConfigurations.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StackConfiguration{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", brickConfigurations=" + brickConfigurations +
                ", loadBalancerIp=" + loadBalancerIp +
                ", scmSshPort=" + scmSshPort +
                '}';
    }

    private void checkIfConfigurationExist(BrickType expectedBrick, Set<BrickConfiguration> brickConfigurations) {
        assert expectedBrick != null : "expectedBrick must be defined";
        assert brickConfigurations != null : "brickConfigurations must be defined";

        if (expectedBrick.isRequiered()) {
            Iterator<BrickConfiguration> iterator = brickConfigurations.iterator();
            boolean found = false;
            while (!found && iterator.hasNext()) {
                BrickConfiguration brickConfiguration = iterator.next();
                found = brickConfiguration != null && expectedBrick.equals(brickConfiguration.getType());
            }

            if (!found) {
                throw new IllegalArgumentException("brickConfigurations " + StringUtils.join(brickConfigurations, ",") + " not contain " + expectedBrick);
            }
        }
    }
}
