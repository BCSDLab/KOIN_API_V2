package in.koreatech.koin.domain.coop.model;

import in.koreatech.koin.domain.dining.model.DiningType;

public record DiningImageUploadEvent(
    String imageUrl,
    DiningType diningType
) {
}
