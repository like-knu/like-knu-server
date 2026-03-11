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
import java.util.List;
import java.util.regex.Pattern;

public class SlackErrorAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"));

    @Setter
    private String webhookUrl;

    @Setter
    private String excludePatterns;

    private List<Pattern> compiledExcludePatterns;

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

        sendSlackMessage(event, fullMessage);
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

    private void sendSlackMessage(ILoggingEvent event, String fullMessage) {
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

            String slackMessage = String.format(
                    ":rotating_light: *ERROR*  `%s`\n>%s\n\n*Logger:* `%s`\n*Time:* %s%s",
                    shortLogger,
                    event.getFormattedMessage(),
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

            Slack.getInstance().send(webhookUrl, payload);
        } catch (Exception e) {
            addError("Slack 알림 전송 실패", e);
        }
    }
}
