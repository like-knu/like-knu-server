package com.woopaca.likeknu.controller.dto.banner;

import com.woopaca.likeknu.entity.HomeBanner;
import lombok.Builder;

@Builder
public record HomeBannerResponse(Long bannerId, String title, String body, String linkPath, String minAppVersion) {

    public static HomeBannerResponse of(HomeBanner banner) {
        return HomeBannerResponse.builder()
                .bannerId(banner.getId())
                .title(banner.getTitle())
                .body(banner.getBody())
                .linkPath(banner.getLinkPath())
                .minAppVersion(banner.getMinAppVersion())
                .build();
    }
}
