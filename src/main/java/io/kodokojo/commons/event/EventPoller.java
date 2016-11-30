package io.kodokojo.commons.event;

import java.util.List;
import java.util.Set;

public interface EventPoller {

    List<Event> poll();

}
