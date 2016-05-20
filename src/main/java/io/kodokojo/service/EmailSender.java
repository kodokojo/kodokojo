package io.kodokojo.service;

import java.util.List;

public interface EmailSender {

    void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent);

    default void send(List<String> to, List<String> cc, List<String> ci, String object, String content) {
        send(to,cc,ci, object, content, false);
    }



}
