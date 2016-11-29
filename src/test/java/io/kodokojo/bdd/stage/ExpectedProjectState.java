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
package io.kodokojo.bdd.stage;

import io.kodokojo.commons.model.BrickType;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ExpectedProjectState {

    private final List<String> stackNamePresents;

    private final List<BrickType> brickTypePresents;

    public ExpectedProjectState(List<String> stackNamePresents, List<BrickType> brickTypePresents) {
        this.stackNamePresents = stackNamePresents;
        this.brickTypePresents = brickTypePresents;
    }

    public List<String> getStackNamePresents() {
        return stackNamePresents;
    }

    public List<BrickType> getBrickTypePresents() {
        return brickTypePresents;
    }

    @Override
    public String toString() {
        return StringUtils.join(brickTypePresents, ",");
    }
}