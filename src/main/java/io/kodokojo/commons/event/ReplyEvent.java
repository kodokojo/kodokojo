package io.kodokojo.commons.event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class ReplyEvent {

    private final String correlationId;

    private final CountDownLatch replyDefined;

    private long timeout;

    private Event reply;

    public ReplyEvent(String correlationId) {
        if (isBlank(correlationId)) {
            throw new IllegalArgumentException("correlationId must be defined.");
        }
        this.correlationId = correlationId;
        this.replyDefined = new CountDownLatch(1);
    }

    public void setReply(Event reply) {
        requireNonNull(reply, "reply must be defined.");
        this.reply = reply;
        replyDefined.countDown();
    }

    public String getCorrelationId() {
        return correlationId;
    }


    public long getTimeout() {
        return timeout;
    }

    public Event getReply(long time, TimeUnit timeUnit) throws InterruptedException {
        timeout = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, timeUnit);
        replyDefined.await(time, timeUnit);
        return reply;
    }
}
