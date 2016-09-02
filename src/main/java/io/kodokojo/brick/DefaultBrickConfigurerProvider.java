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

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.brick.dockerregistry.DockerRegistryConfigurer;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.jenkins.JenkinsConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;

import javax.inject.Inject;

public class DefaultBrickConfigurerProvider implements BrickConfigurerProvider {

    private final BrickUrlFactory brickUrlFactory;

    private final OkHttpClient okHttpClient;

    @Inject
    public DefaultBrickConfigurerProvider(BrickUrlFactory brickUrlFactory, OkHttpClient okHttpClient) {
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        if (okHttpClient == null) {
            throw new IllegalArgumentException("okHttpClient must be defined.");
        }
        this.brickUrlFactory = brickUrlFactory;
        this.okHttpClient = okHttpClient;
    }

    @Override
    public BrickConfigurer provideFromBrick(BrickConfiguration brickConfiguration) {
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        switch (brickConfiguration.getName()) {
            case DefaultBrickFactory.GITLAB:
                return new GitlabConfigurer(brickUrlFactory);
            case DefaultBrickFactory.JENKINS:
                return new JenkinsConfigurer(okHttpClient);
            case DefaultBrickFactory.NEXUS:
                return new NexusConfigurer(okHttpClient);
            case DefaultBrickFactory.DOCKER_REGISTRY:
                return new DockerRegistryConfigurer();
            default:
                return null;
        }
    }
}
