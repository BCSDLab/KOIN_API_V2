package in.koreatech.koin.domain.dining.model;

import lombok.Getter;

@Getter
public enum DiningPlace {
    A코너("A코너", true),
    B코너("B코너", true),
    C코너("C코너", true),
    능수관("능수관", false),
    _2캠퍼스("2캠퍼스", false);

    private final String placeName;
    private final boolean isCafeteria;

    DiningPlace(String placeName, boolean isCafeteria) {
        this.placeName = placeName;
        this.isCafeteria = isCafeteria;
    }
}
