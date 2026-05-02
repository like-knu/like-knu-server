package com.woopaca.likeknu.service;

import com.woopaca.likeknu.controller.dto.keyword.request.KeywordCreateRequest;
import com.woopaca.likeknu.controller.dto.keyword.response.KeywordResponse;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.entity.Keyword;
import com.woopaca.likeknu.exception.BusinessException;
import com.woopaca.likeknu.repository.DeviceRepository;
import com.woopaca.likeknu.repository.KeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class KeywordService {

    private static final int MAX_KEYWORDS_PER_DEVICE = 10;

    private final KeywordRepository keywordRepository;
    private final DeviceRepository deviceRepository;

    public KeywordService(KeywordRepository keywordRepository, DeviceRepository deviceRepository) {
        this.keywordRepository = keywordRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> getKeywords(String deviceId) {
        Device device = findDevice(deviceId);
        return keywordRepository.findAllByDeviceOrderByCreatedAtAsc(device).stream()
                .map(KeywordResponse::from)
                .toList();
    }

    public void addKeyword(KeywordCreateRequest request) {
        Device device = findDevice(request.deviceId());

        Keyword keyword;
        try {
            keyword = Keyword.of(device, request.keyword());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }

        if (keywordRepository.findByDeviceAndKeyword(device, keyword.getKeyword()).isPresent()) {
            throw new BusinessException("이미 등록된 키워드입니다.");
        }
        if (keywordRepository.countByDevice(device) >= MAX_KEYWORDS_PER_DEVICE) {
            throw new BusinessException(String.format("키워드는 최대 %d개까지 등록할 수 있습니다.", MAX_KEYWORDS_PER_DEVICE));
        }

        keywordRepository.save(keyword);
    }

    public void removeKeyword(String deviceId, String keyword) {
        Device device = findDevice(deviceId);
        String normalized = Keyword.normalize(keyword);
        Keyword target = keywordRepository.findByDeviceAndKeyword(device, normalized)
                .orElseThrow(() -> new BusinessException("등록되지 않은 키워드입니다."));
        keywordRepository.delete(target);
    }

    private Device findDevice(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("Device not found! [%s]", deviceId)));
    }
}
