package in.koreatech.koin.domain.coop.model;

import in.koreatech.koin.domain.dining.model.DiningType;

public record DiningSoldOutEvent(
    int id,
    String place,
    DiningType diningType
) {

}
