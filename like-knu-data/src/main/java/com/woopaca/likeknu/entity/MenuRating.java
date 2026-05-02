package com.woopaca.likeknu.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Table(name = "menu_rating")
@Entity
public class MenuRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;

    private Timestamp ratedAt;

    @JoinColumn(name = "device")
    @ManyToOne(fetch = FetchType.LAZY)
    private Device device;

    @JoinColumn(name = "menu")
    @ManyToOne(fetch = FetchType.LAZY)
    private Menu menu;

    protected MenuRating() {
    }

    @Builder
    public MenuRating(int rating, Device device, Menu menu) {
        this.rating = rating;
        this.ratedAt = new Timestamp(System.currentTimeMillis());
        this.device = device;
        this.menu = menu;
    }

    public void changeRating(int rating) {
        this.rating = rating;
        this.ratedAt = new Timestamp(System.currentTimeMillis());
    }
}
