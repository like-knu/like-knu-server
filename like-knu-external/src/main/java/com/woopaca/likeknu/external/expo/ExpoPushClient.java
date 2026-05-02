package com.woopaca.likeknu.external.expo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class ExpoPushClient {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final int BATCH_SIZE = 100;

    private final RestClient restClient;

    public ExpoPushClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Async
    public void sendAsync(List<ExpoPushMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            List<ExpoPushMessage> chunk = messages.subList(i, Math.min(i + BATCH_SIZE, messages.size()));
            send(chunk);
        }
    }

    private void send(List<ExpoPushMessage> chunk) {
        try {
            String response = restClient.post()
                    .uri(EXPO_PUSH_URL)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept-Encoding", "gzip, deflate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(chunk)
                    .retrieve()
                    .body(String.class);
            log.info("[ExpoPushClient] {}건 전송 완료. response: {}", chunk.size(), response);
        } catch (Exception e) {
            log.warn("[ExpoPushClient] Expo 푸시 전송 실패. size: {}, error: {}", chunk.size(), e.getMessage());
        }
    }
}
