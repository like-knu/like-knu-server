package com.woopaca.likeknu.controller;

import com.woopaca.likeknu.controller.dto.base.ResponseDto;
import com.woopaca.likeknu.controller.dto.keyword.request.KeywordCreateRequest;
import com.woopaca.likeknu.controller.dto.keyword.response.KeywordResponse;
import com.woopaca.likeknu.service.KeywordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/keywords")
@RestController
public class KeywordController {

    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping
    public ResponseDto<List<KeywordResponse>> getKeywords(@RequestParam("deviceId") String deviceId) {
        return ResponseDto.of(keywordService.getKeywords(deviceId));
    }

    @PostMapping
    public ResponseDto<String> addKeyword(@RequestBody KeywordCreateRequest request) {
        keywordService.addKeyword(request);
        return ResponseDto.of("키워드가 등록되었습니다.");
    }

    @DeleteMapping
    public ResponseDto<String> removeKeyword(
            @RequestParam("deviceId") String deviceId,
            @RequestParam("keyword") String keyword
    ) {
        keywordService.removeKeyword(deviceId, keyword);
        return ResponseDto.of("키워드가 삭제되었습니다.");
    }
}
