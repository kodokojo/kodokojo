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
package io.kodokojo.endpoint;

import io.kodokojo.brick.BrickFactory;
import io.kodokojo.endpoint.dto.BrickConfigDto;
import io.kodokojo.model.Brick;
import io.kodokojo.service.authentification.SimpleCredential;
import spark.Spark;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.get;

public class BrickSparkEndpoint extends AbstractSparkEndpoint {

    private final BrickFactory brickFactory;

    @Inject
    public BrickSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, BrickFactory brickFactory) {
        super(userAuthenticator);
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        this.brickFactory = brickFactory;
    }

    @Override
    public void configure() {

        get(BASE_API + "/brick", JSON_CONTENT_TYPE, ((request, response) -> {
            List<Brick> bricks = brickFactory.listBrickAvailable();
            List<BrickConfigDto> result = bricks.stream().map(b -> new BrickConfigDto(b.getName(), b.getType().name(), b.getVersion())).collect(Collectors.toList());
            return result;
        }), jsonResponseTransformer);

    }
}
