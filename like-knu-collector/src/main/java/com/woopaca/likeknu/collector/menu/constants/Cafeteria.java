package com.woopaca.likeknu.collector.menu.constants;

import lombok.Getter;

@Getter
public enum Cafeteria implements CafeteriaProxy {

    SINGWAN_STUDENT_CAFETERIA(Campus.SINGWAN, "소담", 18),
    SINGWAN_EMPLOYEES_RESTAURANT(Campus.SINGWAN, "늘솜", 19),
    SINGWAN_EANHANGSA(Campus.SINGWAN, "홍/은/해", 23),
    SINGWAN_VISION(Campus.SINGWAN, "비전/블룸", 24),
    SINGWAN_DREAM(Campus.SINGWAN, "드림", 25),

    CHEONAN_STUDENT_CAFETERIA(Campus.CHEONAN, "학생식당", 20),
    CHEONAN_EMPLOYEES_RESTAURANT(Campus.CHEONAN, "직원식당", 21),
    CHEONAN_DORMITORY(Campus.CHEONAN, "생활관식당", 26),

    YESAN_STUDENT_CAFETERIA(Campus.YESAN, "학생식당", 16),
    YESAN_EMPLOYEES_RESTAURANT(Campus.YESAN, "직원식당", 17);

    private final Campus campus;
    private final String name;
    private final int number;

    Cafeteria(Campus campus, String name, int number) {
        this.campus = campus;
        this.name = name;
        this.number = number;
    }
}
