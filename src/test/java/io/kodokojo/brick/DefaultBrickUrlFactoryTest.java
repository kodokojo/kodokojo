package io.kodokojo.brick;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBrickUrlFactoryTest {



    @Test
    public void without_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("ACME", "CI");
        assertThat(forgedUrl).isEqualTo("ci-acme.kodokojo.dev");
    }

    @Test
    public void with_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("cqfd","ACME", "CI");
        assertThat(forgedUrl).isEqualTo("ci-acme-cqfd.kodokojo.dev");
    }

}