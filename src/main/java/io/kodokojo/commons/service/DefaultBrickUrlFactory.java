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
package io.kodokojo.commons.service;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultBrickUrlFactory implements BrickUrlFactory {

    public static final String DOMAIN_FORMAT_WITHOUT_ENTITY = "%s-%s.%s";

    public static final String DOMAIN_FORMAT_WITH_ENTITY = "%s-%s-%s.%s";

    private final String baseDomainName;

    public DefaultBrickUrlFactory(String baseDomainName) {
        if (isBlank(baseDomainName)) {
            throw new IllegalArgumentException("baseDomainName must be defined.");
        }
        this.baseDomainName = baseDomainName;
    }

    @Override
    public String forgeUrl(String entity, String projectName, String stackName, String brickType, String brickName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(brickType)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        if (StringUtils.isBlank(entity)) {
            return String.format(DOMAIN_FORMAT_WITHOUT_ENTITY, brickName.toLowerCase(), projectName.toLowerCase(), baseDomainName);
        } else {
            return String.format(DOMAIN_FORMAT_WITH_ENTITY, brickName.toLowerCase(), projectName.toLowerCase(), entity.toLowerCase(), baseDomainName);
        }
    }
}
