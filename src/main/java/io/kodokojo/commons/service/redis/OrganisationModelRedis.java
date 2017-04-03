/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.service.redis;

import io.kodokojo.commons.service.repository.store.OrganisationStoreModel;

import java.io.Serializable;
import java.util.List;

class OrganisationModelRedis implements Serializable {

    private final String identifier;

    private final String name;

    private final boolean concrete;

    private final List<String> projectConfigurationIds;

    public OrganisationModelRedis(OrganisationStoreModel model) {
        this.identifier = model.getIdentifier();
        this.name = model.getName();
        this.concrete = model.isConcrete();
        this.projectConfigurationIds = model.getProjectConfigurations();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public List<String> getProjectConfigurationIds() {
        return projectConfigurationIds;
    }
}
