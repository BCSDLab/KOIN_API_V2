package in.koreatech.koin.domain.dining.model;

import java.time.LocalTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum DiningType {
    BREAKFAST(Map.of("startTime", LocalTime.of(8, 30), "endTime", LocalTime.of(9, 30))),
    LUNCH(Map.of("startTime", LocalTime.of(11, 30), "endTime", LocalTime.of(13, 30))),
    DINNER(Map.of("startTime", LocalTime.of(18, 30), "endTime", LocalTime.of(19, 30)));;

    private final Map<String, LocalTime> diningTime;

    DiningType(Map<String, LocalTime> diningTime) {
        this.diningTime = diningTime;
    }

    @JsonCreator
    public static Map<String, LocalTime> getDiningTime(String diningType) {
        return DiningType.valueOf(diningType.toUpperCase()).getDiningTime();
    }
}
