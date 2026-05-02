package com.woopaca.likeknu.external.expo;

import java.util.Map;

public record ExpoPushMessage(
        String to,
        String title,
        String body,
        String sound,
        Map<String, String> data
) {

    public static ExpoPushMessage of(String to, String title, String body, Map<String, String> data) {
        return new ExpoPushMessage(to, title, body, "default", data);
    }
}
