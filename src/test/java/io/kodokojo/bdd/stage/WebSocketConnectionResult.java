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
package io.kodokojo.bdd.stage;

import javax.websocket.Session;

public class WebSocketConnectionResult {

    private final Session session;

    private final WebSocketEventsListener listener;

    public WebSocketConnectionResult(Session session, WebSocketEventsListener listener) {
        if (session == null) {
            throw new IllegalArgumentException("session must be defined.");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        this.session = session;
        this.listener = listener;
    }

    public Session getSession() {
        return session;
    }

    public WebSocketEventsListener getListener() {
        return listener;
    }

}
