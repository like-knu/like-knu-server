package com.woopaca.likeknu.controller.dto.keyword.response;

import com.woopaca.likeknu.entity.Keyword;

import java.time.LocalDateTime;

public record KeywordResponse(String keyword, LocalDateTime createdAt) {

    public static KeywordResponse from(Keyword keyword) {
        return new KeywordResponse(keyword.getKeyword(), keyword.getCreatedAt());
    }
}
