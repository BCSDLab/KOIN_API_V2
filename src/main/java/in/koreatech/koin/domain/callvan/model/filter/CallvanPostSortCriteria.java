package in.koreatech.koin.domain.callvan.model.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallvanPostSortCriteria {
    LATEST("LATEST"),
    DEPARTURE("DEPARTURE");

    private final String value;
}
