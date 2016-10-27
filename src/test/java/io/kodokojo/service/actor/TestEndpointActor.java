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
package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.actor.project.BrickPropertyToBrickConfigurationActor;
import io.kodokojo.service.actor.project.BrickUpdateUserActor;

import java.util.HashSet;
import java.util.Set;

public  class TestEndpointActor extends AbstractActor {

    private final Set<Object> messages = new HashSet<>();

    public TestEndpointActor() {
        receive(ReceiveBuilder.match(BrickUpdateUserActor.BrickUpdateUserMsg.class, msg -> {
            messages.add(msg);
            sender().tell(new BrickUpdateUserActor.BrickUpdateUserResultMsg(msg, true), self());
        }).match(BrickPropertyToBrickConfigurationActor.BrickPropertyToBrickConfigurationMsg.class, msg -> {
            messages.add(msg);
            sender().tell(new BrickPropertyToBrickConfigurationActor.BrickPropertyToBrickConfigurationResultMsg(true), self());
        }).matchAny(messages::add).build());
    }

    public void cleanMessages() {
        messages.clear();;
    }

    public Set<Object> getMessages() {
        return new HashSet<>(messages);
    }
}