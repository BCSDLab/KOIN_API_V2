package in.koreatech.koin.domain.dining.model;

import java.time.LocalTime;
import java.util.Arrays;

import in.koreatech.koin.domain.dining.exception.DiningTypeNotFoundException;
import lombok.Getter;

@Getter
public enum DiningType {
    BREAKFAST("아침", LocalTime.of(8, 0), LocalTime.of(9, 30)),
    LUNCH("점심", LocalTime.of(11, 30), LocalTime.of(13, 30)),
    DINNER("저녁", LocalTime.of(17, 30), LocalTime.of(18, 30)),
    ;

    private final String diningName;
    private final LocalTime startTime;
    private final LocalTime endTime;

    DiningType(String label, LocalTime startTime, LocalTime endTime) {
        this.diningName = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static DiningType from(String diningType) {
        return Arrays.stream(values())
            .filter(it ->
                it.diningName.equalsIgnoreCase(diningType) ||
                    it.name().equalsIgnoreCase(diningType)
            )
            .findAny()
            .orElseThrow(() -> DiningTypeNotFoundException.withDetail(diningType));
    }
}
