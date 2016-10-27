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