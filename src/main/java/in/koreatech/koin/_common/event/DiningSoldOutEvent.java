package in.koreatech.koin._common.event;

import in.koreatech.koin.domain.dining.model.DiningType;

public record DiningSoldOutEvent(
    int id,
    String place,
    DiningType diningType
) {

}
