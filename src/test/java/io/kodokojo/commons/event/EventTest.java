package io.kodokojo.commons.event;

import org.junit.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void json_converter_string_event_test() {
        Event fakeEvent = buildTestEvent("Coucou");
        String json = Event.convertToJson(fakeEvent);
        System.out.println(json);
        Event fromJson = Event.buildFromJson(json);
        assertThat(fromJson).isNotNull();
        assertThat(fromJson.getCategory()).isEqualTo(Event.Category.TECHNICAL);
        assertThat(fromJson.getEventType()).isEqualTo("test");
        assertThat(fromJson.getPayload()).isEqualTo("Coucou");
    }


    public static Event buildTestEvent(String payload) {
        requireNonNull(payload, "payload must be defined.");
        Event.Header headers = new Event.Header(Event.Category.TECHNICAL, "tester", System.currentTimeMillis(), "test");
        return new Event(headers, payload);
    }

}