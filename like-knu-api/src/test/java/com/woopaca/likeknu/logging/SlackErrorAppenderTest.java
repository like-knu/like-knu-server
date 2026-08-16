package com.woopaca.likeknu.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlackErrorAppenderTest {

    private SlackErrorAppender appender;
    private Slack mockSlack;

    @BeforeEach
    void setUp() throws IOException {
        mockSlack = mock(Slack.class);
        WebhookResponse response = WebhookResponse.builder()
                .code(200)
                .message("OK")
                .body("ok")
                .build();
        when(mockSlack.send(any(), any(Payload.class))).thenReturn(response);

        appender = new SlackErrorAppender();
        appender.setWebhookUrl("https://hooks.slack.com/services/test");
        appender.setExcludePatterns("IOException: Broken pipe|Failed to fetch Naver real-time bus information.");
        appender.setTimeoutPatterns("TimeoutException|SocketTimeoutException|ConnectTimeoutException|ReadTimeoutHandler|timed out");
        appender.setTimeoutWindowMinutes(180);
        appender.setTimeoutThreshold(15);
        appender.setTimeoutCooldownMinutes(60);
        appender.setSlack(mockSlack);
        appender.start();
    }

    private ILoggingEvent createMockEvent(String message, long timestamp, IThrowableProxy throwableProxy) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLoggerName()).thenReturn("com.woopaca.likeknu.TestLogger");
        when(event.getTimeStamp()).thenReturn(timestamp);
        when(event.getThrowableProxy()).thenReturn(throwableProxy);
        return event;
    }

    @Test
    @DisplayName("일반 에러 발생 시 즉시 Slack 알림이 전송된다.")
    void normalError_SendsImmediately() throws IOException {
        // given
        ILoggingEvent event = createMockEvent("NullPointerException occurred", 1000L, null);

        // when
        appender.append(event);

        // then
        ArgumentCaptor<Payload> payloadCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(mockSlack, times(1)).send(eq("https://hooks.slack.com/services/test"), payloadCaptor.capture());
        Payload payload = payloadCaptor.getValue();
        assertThat(payload.getBlocks().get(0).toString()).contains("ERROR");
        assertThat(payload.getBlocks().get(0).toString()).doesNotContain("TIMEOUT 임계치 초과");
    }

    @Test
    @DisplayName("제외 패턴(ExcludePatterns)에 매칭되는 로그는 알림이 전송되지 않는다.")
    void excludePatterns_DoesNotSend() throws IOException {
        // given
        ILoggingEvent event = createMockEvent("ClientAbortException: IOException: Broken pipe", 1000L, null);

        // when
        appender.append(event);

        // then
        verify(mockSlack, never()).send(any(), any(Payload.class));
    }

    @Test
    @DisplayName("Timeout 에러는 15회 미만 발생 시 알림이 전송되지 않는다.")
    void timeoutError_BelowThreshold_DoesNotSend() throws IOException {
        // given & when (14회 발생)
        long baseTime = System.currentTimeMillis();
        for (int i = 1; i <= 14; i++) {
            ILoggingEvent event = createMockEvent("SocketTimeoutException: connect timed out", baseTime + (i * 1000), null);
            appender.append(event);
        }

        // then
        verify(mockSlack, never()).send(any(), any(Payload.class));
    }

    @Test
    @DisplayName("Timeout 에러가 3시간 내 15회 발생 시 임계치 초과 알림이 전송된다.")
    void timeoutError_ReachesThreshold_SendsAlert() throws IOException {
        // given
        long baseTime = System.currentTimeMillis();
        for (int i = 1; i <= 14; i++) {
            ILoggingEvent event = createMockEvent("SocketTimeoutException: Read timed out", baseTime + (i * 1000), null);
            appender.append(event);
        }

        // when (15번째 발생)
        ILoggingEvent event15 = createMockEvent("SocketTimeoutException: Read timed out", baseTime + 15000, null);
        appender.append(event15);

        // then
        ArgumentCaptor<Payload> payloadCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(mockSlack, times(1)).send(eq("https://hooks.slack.com/services/test"), payloadCaptor.capture());
        Payload payload = payloadCaptor.getValue();
        String messageContent = payload.getBlocks().get(0).toString();
        assertThat(messageContent).contains("TIMEOUT 임계치 초과");
        assertThat(messageContent).contains("15회");
    }

    @Test
    @DisplayName("Timeout 알림 전송 후 쿨다운(60분) 이내에 추가 발생 시 알림이 억제된다.")
    void timeoutError_WithinCooldown_SuppressesAlert() throws IOException {
        // given (15회 발생으로 1회 알림 완료)
        long baseTime = System.currentTimeMillis();
        for (int i = 1; i <= 15; i++) {
            ILoggingEvent event = createMockEvent("ConnectTimeoutException: connection timed out", baseTime + (i * 1000), null);
            appender.append(event);
        }
        verify(mockSlack, times(1)).send(any(), any(Payload.class));

        // when (15분 뒤 16번째 발생 - 쿨다운 60분 이내)
        long withinCooldownTime = baseTime + (15 * 60 * 1000L);
        ILoggingEvent event16 = createMockEvent("ConnectTimeoutException: connection timed out", withinCooldownTime, null);
        appender.append(event16);

        // then (추가 발송 없이 총 1회 유지)
        verify(mockSlack, times(1)).send(any(), any(Payload.class));
    }

    @Test
    @DisplayName("Timeout 알림 전송 후 쿨다운(60분) 경과 후 여전히 윈도우 내 15회 이상이면 다시 알림이 전송된다.")
    void timeoutError_AfterCooldown_SendsAlertAgain() throws IOException {
        // given (15회 발생으로 1회 알림 완료)
        long baseTime = System.currentTimeMillis();
        for (int i = 1; i <= 15; i++) {
            ILoggingEvent event = createMockEvent("ConnectTimeoutException: connection timed out", baseTime + (i * 1000), null);
            appender.append(event);
        }
        verify(mockSlack, times(1)).send(any(), any(Payload.class));

        // when (65분 뒤 16번째 발생 - 3시간 윈도우 내이며 쿨다운 60분 경과)
        long afterCooldownTime = baseTime + (65 * 60 * 1000L);
        ILoggingEvent event16 = createMockEvent("ConnectTimeoutException: connection timed out", afterCooldownTime, null);
        appender.append(event16);

        // then (총 2회 발송)
        verify(mockSlack, times(2)).send(any(), any(Payload.class));
    }

    @Test
    @DisplayName("3시간(윈도우)이 지난 오래된 Timeout 에러는 카운트에서 제외된다.")
    void timeoutError_OldEventsEvictedFromWindow() throws IOException {
        // given (14회 발생)
        long baseTime = 1_000_000_000L;
        for (int i = 1; i <= 14; i++) {
            ILoggingEvent event = createMockEvent("SocketTimeoutException: timed out", baseTime + (i * 1000), null);
            appender.append(event);
        }

        // when (3시간 1분 = 181분 뒤에 1회 발생 -> 이전 14회는 윈도우 밖으로 만료됨)
        long futureTime = baseTime + (181 * 60 * 1000L);
        ILoggingEvent futureEvent = createMockEvent("SocketTimeoutException: timed out", futureTime, null);
        appender.append(futureEvent);

        // then (현재 윈도우 내 1회이므로 알림 미발송)
        verify(mockSlack, never()).send(any(), any(Payload.class));
    }
}
