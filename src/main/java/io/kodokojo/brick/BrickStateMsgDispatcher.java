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
package io.kodokojo.brick;

import io.kodokojo.model.BrickState;

import java.util.HashSet;
import java.util.Set;

public class BrickStateMsgDispatcher implements BrickStateMsgListener {

    private final Set<BrickStateMsgListener> listeners;

    public BrickStateMsgDispatcher() {
        this.listeners = new HashSet<>();
    }

    public void addListener(BrickStateMsgListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        this.listeners.add(listener);
    }

    @Override
    public void receive(BrickState brickState) {
        listeners.forEach(listener -> listener.receive(brickState));
    }
}
