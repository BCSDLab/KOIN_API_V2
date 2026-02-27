package in.koreatech.koin.domain.callvan.model.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallvanPostSortCriteria {
    DEPARTURE_ASC("DEPARTURE_ASC"),
    DEPARTURE_DESC("DEPARTURE_DESC"),
    LATEST_ASC("LATEST_ASC"),
    LATEST_DESC("LATEST_DESC");

    private final String value;
}
