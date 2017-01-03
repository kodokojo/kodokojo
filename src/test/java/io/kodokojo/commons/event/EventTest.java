/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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