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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;

public class GsonEventSerializerTest {

    @Test
    public void deserialize_event() {
        String input = "{\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"headers\": {\n" +
                "    \"category\": \"BUSINESS\",\n" +
                "    \"from\": \"api@737b46e7-7668-44f3-9f93-f8000bceaaaa\",\n" +
                "    \"replyTo\": \"api-737b46e7-7668-44f3-9f93-f8000bceaaaa\",\n" +
                "    \"correlationId\": \"5ce1e029-b477-4295-b549-a17d327037d5\",\n" +
                "    \"creationDate\": 1480978336019,\n" +
                "    \"eventType\": \"user_creation_request\",\n" +
                "    \"custom\": {}\n" +
                "  },\n" +
                "  \"payload\": {\"id\":\"d9684b30a0e98b18821803e3d6f622b5816a447e\",\"email\":\"aletaxin@kodokojo.io\",\"username\":\"aletaxin\",\"entityId\":\"f78312857a2eff51507e7cfc702971dc936e8c32\"}\n" +
                "}";
        GsonEventSerializer gsonEventSerializer = new GsonEventSerializer();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(input);
        Event event = gsonEventSerializer.deserialize(jsonElement, null, null);
        System.out.println(event);
    }

    @Test
    public void serialize_payload_object() {
        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setFrom("test");
        eventBuilder.setEventType("test_request");
        eventBuilder.setCorrelationId("1234");
        eventBuilder.setReplyTo("root@test");
        eventBuilder.setCategory(Event.Category.BUSINESS);
        eventBuilder.setJsonPayload("{\"id\":\"d9684b30a0e98b18821803e3d6f622b5816a447e\",\"email\":\"aletaxin@kodokojo.io\",\"username\":\"aletaxin\",\"entityId\":\"f78312857a2eff51507e7cfc702971dc936e8c32\"}");
        GsonEventSerializer gsonEventSerializer = new GsonEventSerializer();
        JsonElement serialize = gsonEventSerializer.serialize(eventBuilder.build(), null, null);
        System.out.println(serialize);
    }

    @Test
    public void serialize_payload_string() {
        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setFrom("test");
        eventBuilder.setEventType("test_request");
        eventBuilder.setCorrelationId("1234");
        eventBuilder.setReplyTo("root@test");
        eventBuilder.setCategory(Event.Category.BUSINESS);
        eventBuilder.setJsonPayload("d9684b30a0e98b18821803e3d6f622b5816a447e");
        GsonEventSerializer gsonEventSerializer = new GsonEventSerializer();
        JsonElement serialize = gsonEventSerializer.serialize(eventBuilder.build(), null, null);
        System.out.println(serialize);
    }

}