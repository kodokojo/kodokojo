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
package io.kodokojo.service;

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.brick.*;
import io.kodokojo.model.Brick;
import io.kodokojo.model.BrickType;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.jenkins.JenkinsConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBrickConfigurerProviderTest {

    private BrickFactory brickFactory;

    private BrickConfigurerProvider brickConfigurerProvider;

    @Before
    public void setup() {
        brickFactory = new DefaultBrickFactory();
        brickConfigurerProvider = new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"), new OkHttpClient());
    }

    @Test
    public void unexpected_brick_type() {
        BrickConfigurer unknow = brickConfigurerProvider.provideFromBrick(new Brick("unknow", BrickType.ALTERTING, "1.0"));
        assertThat(unknow).isNull();
    }

    @Test
    public void get_jenkins_brick_configurer() {
        tests("jenkins", JenkinsConfigurer.class);
    }

    @Test
    public void get_gitlab_brick_configurer() {
        tests("gitlab", GitlabConfigurer.class);
    }

    @Test
    public void get_nexus_brick_configurer() {
        tests("nexus", NexusConfigurer.class);
    }

    private void tests(String brickName, Class expectedClass) {
        BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(brickFactory.createBrick(brickName));
        assertThat(brickConfigurer).isInstanceOf(expectedClass);
    }

}