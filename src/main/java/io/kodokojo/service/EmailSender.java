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
package io.kodokojo.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public interface EmailSender {

    String MINE_TEXT_HTML = "text/html";

    String MINE_TEXT_PLAIN = "text/plain";

    void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent, Set<Attachment> attachments);

    default void send(List<String> to, List<String> cc, List<String> ci, String object, String content, Set<Attachment> attachments) {
        send(to, cc, ci, object, content, true, attachments);
    }

    default void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent) {
        send(to, cc, ci, object, content, false, null);
    }

    default void send(List<String> to, List<String> cc, List<String> ci, String object, String content) {
        send(to, cc, ci, object, content, false);
    }

    abstract class Attachment<T> {

        private final String id;

        private final T content;

        private final String fileName;

        public Attachment(String id, T content, String fileName) {
            if (isBlank(id)) {
                id = UUID.randomUUID().toString();
            }
            requireNonNull(content, "content must be defined.");
            if (isBlank(fileName)) {
                throw new IllegalArgumentException("content must be defined.");
            }
            this.id = id;
            this.content = content;
            this.fileName = fileName;
        }

        public Attachment(T content, String fileName) {
            this(null, content, fileName);
        }

        public String getId() {
            return id;
        }

        public T getContent() {
            return content;
        }

        public String getFileName() {
            return fileName;
        }

        public abstract String mineType();
    }

    class PlainTextAttachment<T> extends Attachment<T> {


        public PlainTextAttachment(T content, String fileName) {
            super(content, fileName);
        }

        public PlainTextAttachment(String id, T content, String fileName) {
            super(id, content, fileName);
        }

        @Override
        public String mineType() {
            return MINE_TEXT_PLAIN;
        }
    }


}
