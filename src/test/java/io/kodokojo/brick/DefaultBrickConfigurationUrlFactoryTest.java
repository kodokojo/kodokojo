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
package io.kodokojo.brick;

import io.kodokojo.model.*;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultBrickConfigurationUrlFactoryTest {

    @Test
    public void object_without_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");

        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);
        BrickConfiguration brickConfiguration = new BrickConfiguration("jenkins", BrickType.CI, "1.651", Collections.singleton(new PortDefinition(8080)));

        when(projectConfiguration.getName()).thenReturn("ACME");
        String forgedUrl = brickUrlFactory.forgeUrl(projectConfiguration, "build-A", brickConfiguration);
        assertThat(forgedUrl).isEqualTo("jenkins-acme.kodokojo.dev");
    }

    @Test
    public void object_with_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("cqfd","ACME","build-A", "CI", "jenkins");
        assertThat(forgedUrl).isEqualTo("jenkins-acme-cqfd.kodokojo.dev");
    }

    @Test
    public void string_without_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");

        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);
        BrickConfiguration brickConfiguration = new BrickConfiguration("jenkins", BrickType.CI, "1.651", Collections.singleton(new PortDefinition(8080)));

        when(projectConfiguration.getName()).thenReturn("acme");
        String forgedUrl = brickUrlFactory.forgeUrl(projectConfiguration, "build-A", brickConfiguration);
        assertThat(forgedUrl).isEqualTo("jenkins-acme.kodokojo.dev");
    }

    @Test
    public void string_with_entity_upper_case() {
        BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");
        String forgedUrl = brickUrlFactory.forgeUrl("cqfd","ACME","build-A", "CI", "jenkins");
        assertThat(forgedUrl).isEqualTo("jenkins-acme-cqfd.kodokojo.dev");
    }

}