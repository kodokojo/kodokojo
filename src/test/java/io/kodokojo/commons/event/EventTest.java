package io.kodokojo.commons.event;

import org.junit.Test;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void json_converter_string_event_test() {
        Event<String> fakeEvent = buildTestEvent("Coucou");
        String json = Event.convertToJson(fakeEvent);

        Event<String> fromJson = Event.buildFromJson(String.class, json);
        assertThat(fromJson).isNotNull();
        assertThat(fromJson.getCategory()).isEqualTo(Event.Category.TECHNICAL);
        assertThat(fromJson.getEventType()).isEqualTo("test");
        assertThat(fromJson.getPayload()).isEqualTo("Coucou");
    }

    @Test
    public void json_converter_boolean_event_test() {
        Event fakeEvent = buildTestEvent(Boolean.TRUE);
        String json = Event.convertToJson(fakeEvent);
        Event<Boolean> fromJson = Event.buildFromJson(Boolean.class, json);
        assertThat(fromJson).isNotNull();
        assertThat(fromJson.getCategory()).isEqualTo(Event.Category.TECHNICAL);
        assertThat(fromJson.getEventType()).isEqualTo("test");
        assertThat(fromJson.getPayload()).isEqualTo(Boolean.TRUE);
    }



    public static <T extends Serializable> Event<T> buildTestEvent(T payload) {
        requireNonNull(payload, "payload must be defined.");
        Event.Header headers = new Event.Header(Event.Category.TECHNICAL, "tester", System.currentTimeMillis(), "test");
        return new Event<>(headers, payload);
    }

}