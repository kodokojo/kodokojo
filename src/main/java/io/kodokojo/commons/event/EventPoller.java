package io.kodokojo.commons.event;

import java.util.List;

public interface EventPoller {

    List<Event> poll();

}
