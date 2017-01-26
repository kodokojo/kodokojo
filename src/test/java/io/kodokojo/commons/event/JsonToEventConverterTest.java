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