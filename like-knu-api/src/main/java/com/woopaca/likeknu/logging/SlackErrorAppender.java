package com.woopaca.likeknu.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.webhook.Payload;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public class SlackErrorAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"));

    @Setter
    private String webhookUrl;

    @Setter
    private String excludePatterns;

    @Setter
    private String timeoutPatterns;

    @Setter
    private int timeoutWindowMinutes = 180;

    @Setter
    private int timeoutThreshold = 15;

    @Setter
    private int timeoutCooldownMinutes = 60;

    @Setter
    private Slack slack = Slack.getInstance();

    private List<Pattern> compiledExcludePatterns;
    private List<Pattern> compiledTimeoutPatterns;

    private final Object lock = new Object();
    private final Deque<Long> timeoutTimestamps = new ArrayDeque<>();
    private long lastAlertTimestamp = 0L;

    @Override
    public void start() {
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            compiledExcludePatterns = List.of(excludePatterns.split("\\|")).stream()
                    .map(String::trim)
                    .map(Pattern::compile)
                    .toList();
        } else {
            compiledExcludePatterns = List.of();
        }

        if (timeoutPatterns != null && !timeoutPatterns.isEmpty()) {
            compiledTimeoutPatterns = List.of(timeoutPatterns.split("\\|")).stream()
                    .map(String::trim)
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
        } else {
            compiledTimeoutPatterns = List.of();
        }

        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String fullMessage = buildFullMessage(event);

        for (Pattern pattern : compiledExcludePatterns) {
            if (pattern.matcher(fullMessage).find()) {
                return;
            }
        }

        if (isTimeout(fullMessage)) {
            TimeoutCheckResult checkResult = checkTimeoutThreshold(event.getTimeStamp());
            if (!checkResult.isShouldAlert()) {
                return;
            }
            sendSlackMessage(event, fullMessage, checkResult.getCount());
            return;
        }

        sendSlackMessage(event, fullMessage, null);
    }

    private boolean isTimeout(String fullMessage) {
        if (compiledTimeoutPatterns == null || compiledTimeoutPatterns.isEmpty()) {
            return false;
        }
        for (Pattern pattern : compiledTimeoutPatterns) {
            if (pattern.matcher(fullMessage).find()) {
                return true;
            }
        }
        return false;
    }

    private TimeoutCheckResult checkTimeoutThreshold(long now) {
        synchronized (lock) {
            long windowMillis = timeoutWindowMinutes * 60 * 1000L;
            long cooldownMillis = timeoutCooldownMinutes * 60 * 1000L;

            timeoutTimestamps.addLast(now);
            while (!timeoutTimestamps.isEmpty() && timeoutTimestamps.peekFirst() < now - windowMillis) {
                timeoutTimestamps.pollFirst();
            }

            int count = timeoutTimestamps.size();
            if (count < timeoutThreshold) {
                return TimeoutCheckResult.shouldNotAlert();
            }

            if (now - lastAlertTimestamp < cooldownMillis) {
                return TimeoutCheckResult.shouldNotAlert();
            }

            lastAlertTimestamp = now;
            return TimeoutCheckResult.shouldAlert(count);
        }
    }

    private String buildFullMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getFormattedMessage());

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append("\n").append(ThrowableProxyUtil.asString(throwableProxy));
        }

        return sb.toString();
    }

    private void sendSlackMessage(ILoggingEvent event, String fullMessage, Integer timeoutCount) {
        try {
            String timestamp = FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));
            String loggerName = event.getLoggerName();
            String shortLogger = loggerName.contains(".")
                    ? loggerName.substring(loggerName.lastIndexOf('.') + 1)
                    : loggerName;

            String stackTrace = "";
            if (event.getThrowableProxy() != null) {
                String trace = ThrowableProxyUtil.asString(event.getThrowableProxy());
                stackTrace = trace.length() > 500 ? trace.substring(0, 500) + "..." : trace;
            }

            String header = timeoutCount != null
                    ? String.format(":warning: *[TIMEOUT 임계치 초과]*  `%s`", shortLogger)
                    : String.format(":rotating_light: *ERROR*  `%s`", shortLogger);

            String windowDesc = (timeoutWindowMinutes >= 60 && timeoutWindowMinutes % 60 == 0)
                    ? (timeoutWindowMinutes / 60) + "시간"
                    : timeoutWindowMinutes + "분";

            String countNotice = timeoutCount != null
                    ? String.format("\n:hourglass_flowing_sand: *최근 %s 동안 Timeout 에러가 %d회 발생했습니다.*",
                            windowDesc, timeoutCount)
                    : "";

            String slackMessage = String.format(
                    "%s\n>%s%s\n\n*Logger:* `%s`\n*Time:* %s%s",
                    header,
                    event.getFormattedMessage(),
                    countNotice,
                    loggerName,
                    timestamp,
                    stackTrace.isEmpty() ? "" : "\n\n```\n" + stackTrace + "\n```"
            );

            Payload payload = Payload.builder()
                    .blocks(List.of(SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(slackMessage)
                                    .build())
                            .build()))
                    .build();

            slack.send(webhookUrl, payload);
        } catch (Exception e) {
            addError("Slack 알림 전송 실패", e);
        }
    }

    private static class TimeoutCheckResult {
        private final boolean shouldAlert;
        private final Integer count;

        private TimeoutCheckResult(boolean shouldAlert, Integer count) {
            this.shouldAlert = shouldAlert;
            this.count = count;
        }

        public static TimeoutCheckResult shouldNotAlert() {
            return new TimeoutCheckResult(false, null);
        }

        public static TimeoutCheckResult shouldAlert(int count) {
            return new TimeoutCheckResult(true, count);
        }

        public boolean isShouldAlert() {
            return shouldAlert;
        }

        public Integer getCount() {
            return count;
        }
    }
}

