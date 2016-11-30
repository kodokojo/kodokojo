package io.kodokojo.commons.event;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JsonToEventConverterTest implements JsonToEventConverter {

    @Test
    public void string_payload() {
        String json = generateJson("\"Coucou\"");
        Event stringEvent = converter(json);
        Assertions.assertThat(stringEvent.getPayload()).isEqualTo("Coucou");
    }
    String generateJson(String payload) {
        return "{\"version\":\"1.0.0\",\"headers\":{\"category\":\"TECHNICAL\",\"from\":\"tester\",\"creationDate\":1480452533504,\"eventType\":\"test\",\"custom\":{}},\"payload\":" + payload + "}";
    }

}