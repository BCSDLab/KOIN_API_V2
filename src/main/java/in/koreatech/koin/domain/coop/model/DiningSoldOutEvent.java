package in.koreatech.koin.domain.coop.model;

import in.koreatech.koin.domain.dining.model.DiningType;

public record DiningSoldOutEvent(
    String place,
    // 아, 점, 저 알람 구분을 위해 필요
    DiningType type
) {
}
