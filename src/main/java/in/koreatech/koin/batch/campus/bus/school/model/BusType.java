package in.koreatech.koin.batch.campus.bus.school.model;

import lombok.Getter;

@Getter
public enum BusType {
    COMMUTING("commuting"),
    SHUTTLE("shuttle"),
    ;

    private final String value;

    BusType(String value) {
        this.value = value;
    }
}
