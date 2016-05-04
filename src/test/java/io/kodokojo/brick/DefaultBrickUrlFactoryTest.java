package io.kodokojo.brick;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickType;
import io.kodokojo.model.ProjectConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultBrickUrlFactoryTest {

    @Test
    public void object_without_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");

        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);
        BrickConfiguration brickConfiguration = mock(BrickConfiguration.class);

        when(projectConfiguration.getName()).thenReturn("ACME");
        when(brickConfiguration.getType()).thenReturn(BrickType.CI);

        String forgedUrl = brickUrlFactory.forgeUrl(projectConfiguration, brickConfiguration);
        assertThat(forgedUrl).isEqualTo("ci-acme.kodokojo.dev");
    }

    @Test
    public void object_with_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("cqfd","ACME", "CI");
        assertThat(forgedUrl).isEqualTo("ci-acme-cqfd.kodokojo.dev");
    }

    @Test
    public void string_without_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");

        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);
        BrickConfiguration brickConfiguration = mock(BrickConfiguration.class);

        when(projectConfiguration.getName()).thenReturn("acme");
        when(brickConfiguration.getType()).thenReturn(BrickType.CI);

        String forgedUrl = brickUrlFactory.forgeUrl(projectConfiguration, brickConfiguration);
        assertThat(forgedUrl).isEqualTo("ci-acme.kodokojo.dev");
    }

    @Test
    public void string_with_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("cqfd","ACME", "CI");
        assertThat(forgedUrl).isEqualTo("ci-acme-cqfd.kodokojo.dev");
    }

}