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

public class BrickEndpoint extends AbstractSparkEndpoint {

    private final BrickFactory brickFactory;

    @Inject
    public BrickEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, BrickFactory brickFactory) {
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
