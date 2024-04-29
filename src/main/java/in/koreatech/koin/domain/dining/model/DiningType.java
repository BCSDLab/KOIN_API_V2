package in.koreatech.koin.domain.dining.model;

import java.util.Arrays;

import in.koreatech.koin.domain.dining.exception.DiningTypeNotFoundException;
import lombok.Getter;

@Getter
public enum DiningType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    ;

    private final String label;

    DiningType(String label) {
        this.label = label;
    }

    public static DiningType from(String diningType) {
        return Arrays.stream(values())
            .filter(it -> it.label.equalsIgnoreCase(diningType))
            .findAny()
            .orElseThrow(() -> DiningTypeNotFoundException.withDetail(diningType));
    }
}
