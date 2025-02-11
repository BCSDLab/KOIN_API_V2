package in.koreatech.koin.domain.dining.model.enums;

import static in.koreatech.koin.domain.dining.model.DiningType.*;

import java.util.Arrays;

import in.koreatech.koin.domain.dining.exception.ExcelDiningPositionNotFoundException;
import in.koreatech.koin.domain.dining.model.DiningType;

public enum ExcelDiningPosition {
    BREAKFAST_A_CORNER(BREAKFAST, "A코너", getFirstPosition(), getFirstPosition()),
    BREAKFAST_B_CORNER(BREAKFAST, "B코너", getFirstPosition() + 4, getFirstPosition() + 4),
    BREAKFAST_C_CORNER(BREAKFAST, "C코너", getFirstPosition() + 8, getFirstPosition() + 8),
    BREAKFAST_WELFARE(BREAKFAST, "능수관", 0, getFirstPosition() + 12),
    BREAKFAST_CAMPUS2(BREAKFAST, "2캠퍼스", 0, getFirstPosition() + 14),
    LUNCH_A_CORNER(LUNCH, "A코너", getFirstPosition() + 12, getFirstPosition() + 16),
    LUNCH_B_CORNER(LUNCH, "B코너", getFirstPosition() + 16, getFirstPosition() + 20),
    LUNCH_C_CORNER(LUNCH, "C코너", getFirstPosition() + 20, getFirstPosition() + 24),
    LUNCH_WELFARE(LUNCH, "능수관", 0, getFirstPosition() + 28),
    LUNCH_CAMPUS2(LUNCH, "2캠퍼스", 0, getFirstPosition() + 30),
    DINNER_A_CORNER(DINNER, "A코너", getFirstPosition() + 24, getFirstPosition() + 32),
    DINNER_B_CORNER(DINNER, "B코너", getFirstPosition() + 28, getFirstPosition() + 36),
    DINNER_C_CORNER(DINNER, "C코너", getFirstPosition() + 32, getFirstPosition() + 40),
    DINNER_WELFARE(DINNER, "능수관", 0, getFirstPosition() + 44),
    DINNER_CAMPUS2(DINNER, "2캠퍼스", 0, getFirstPosition() + 46),
    ;

    private final DiningType diningType;
    private final String place;
    private final int startPositionOnlyCafeteria;
    private final int startPositionAllPlace;

    private static int getFirstPosition() {
        return 1;
    }

    ExcelDiningPosition(
        DiningType diningType,
        String place,
        int startPositionOnlyCafeteria,
        int startPositionAllPlace
    ) {
        this.diningType = diningType;
        this.place = place;
        this.startPositionOnlyCafeteria = startPositionOnlyCafeteria;
        this.startPositionAllPlace = startPositionAllPlace;
    }

    public static ExcelDiningPosition from(DiningType diningType, String place) {
        return Arrays.stream(values())
            .filter(position -> position.diningType == diningType && position.place.equals(place))
            .findFirst()
            .orElseThrow(() -> new ExcelDiningPositionNotFoundException(
                "유효하지 않은 ExcelDiningPosition: " + diningType + " and Place: " + place));
    }

    public int getStartPositionOnlyCafeteria(){
        return startPositionOnlyCafeteria;
    }

    public int getStartPositionAllPlace(){
        return startPositionAllPlace;
    }
}
