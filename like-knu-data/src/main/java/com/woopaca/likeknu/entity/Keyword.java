package com.woopaca.likeknu.entity;

import com.woopaca.likeknu.Domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Table(name = "keyword")
@Entity
public class Keyword extends BaseEntity {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Keyword() {
        super(Domain.KEYWORD);
    }

    private Keyword(Device device, String keyword) {
        this();
        this.device = device;
        this.keyword = keyword;
    }

    public static Keyword of(Device device, String keyword) {
        String normalized = normalize(keyword);
        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("키워드는 %d자 이상 %d자 이하여야 합니다.", MIN_LENGTH, MAX_LENGTH));
        }
        return new Keyword(device, normalized);
    }

    public static String normalize(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }
}
