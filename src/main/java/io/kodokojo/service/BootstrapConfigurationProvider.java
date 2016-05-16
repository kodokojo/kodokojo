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

/**
 * Allow to provide informations required to start a Project stack and her Bricks.
 */
public interface BootstrapConfigurationProvider {

    /**
     * Provide the load balancer IP which may used to access to the project stack.
     * @param projectName Name of the project
     * @param stackName The stack name
     * @return Ip of the load balancer
     */
    String provideLoadBalancerIp(String projectName, String stackName);

    /**
     * Provide SSH port configured on the load balancer to access to the stack.
     * @param projectName Name of the project
     * @param stackName The stack name
     * @return SSH port to access to the project stack
     */
    int provideSshPortEntrypoint(String projectName, String stackName);

}
