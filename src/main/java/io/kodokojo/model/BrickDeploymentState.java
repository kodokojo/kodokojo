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



import io.kodokojo.commons.model.Service;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BrickDeploymentState implements Serializable {

    private final Brick brick;

    private final List<Service> services;

    private final int nbInstance;

    public BrickDeploymentState(Brick brick, List<Service> services, int nbInstance) {
        if (brick == null) {
            throw new IllegalArgumentException("brick must be defined.");
        }
       /*
        if (CollectionUtils.isEmpty(services)) {
            throw new IllegalArgumentException("services must be defined.");
        }
        */
        this.brick = brick;
        this.services = services;
        this.nbInstance = nbInstance;
    }

    public Brick getBrick() {
        return brick;
    }

    public List<Service> getServices() {
        return new ArrayList<>(services);
    }

    public int getNbInstance() {
        return nbInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrickDeploymentState that = (BrickDeploymentState) o;

        if (nbInstance != that.nbInstance) return false;
        if (brick != that.brick) return false;
        return services.equals(that.services);

    }

    @Override
    public int hashCode() {
        int result = brick.hashCode();
        result = 31 * result + services.hashCode();
        result = 31 * result + nbInstance;
        return result;
    }

    @Override
    public String toString() {
        return "BrickDeploymentState{" +
                "brick=" + brick +
                ", services=" + services +
                ", nbInstance=" + nbInstance +
                '}';
    }
}
