package io.kodokojo.commons.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class ReplyEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplyEvent.class);

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


        Date end = new Date(timeout);
        SimpleDateFormat df = new SimpleDateFormat();
        LOGGER.debug("Wait reply is defined. timeout:{}", df.format(end));


        boolean await = replyDefined.await(time, timeUnit);
        if (await) {
            LOGGER.debug("Reply received.");
        } else {
            LOGGER.debug("Timeout exceed");
        }

        return reply;
    }
}
