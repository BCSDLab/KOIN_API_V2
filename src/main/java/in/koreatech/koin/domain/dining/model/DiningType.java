package in.koreatech.koin.domain.dining.model;

import java.util.Arrays;
import java.time.LocalTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.dining.exception.DiningTypeNotFoundException;
import lombok.Getter;

@Getter
public enum DiningType {
    BREAKFAST("아침", Map.of("startTime", LocalTime.of(8, 30), "endTime", LocalTime.of(9, 30))),
    LUNCH("점심", Map.of("startTime", LocalTime.of(11, 30), "endTime", LocalTime.of(13, 30))),
    DINNER("저녁", Map.of("startTime", LocalTime.of(18, 30), "endTime", LocalTime.of(19, 30)));;

    private final String label;
    private final Map<String, LocalTime> diningTime;

    DiningType(String label, Map<String, LocalTime> diningTime) {
        this.label = label;
        this.diningTime = diningTime;
    }


    public static DiningType from(String diningType) {
        return Arrays.stream(values())
            .filter(it ->
                it.label.equalsIgnoreCase(diningType) ||
                    it.name().equalsIgnoreCase(diningType)
            )
            .findAny()
            .orElseThrow(() -> DiningTypeNotFoundException.withDetail(diningType));
    }

    @JsonCreator
    public static Map<String, LocalTime> getDiningTime(String diningType) {
        return DiningType.valueOf(diningType.toUpperCase()).getDiningTime();
    }
}
