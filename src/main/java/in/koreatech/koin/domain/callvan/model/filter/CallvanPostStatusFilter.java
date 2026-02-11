package in.koreatech.koin.domain.callvan.model.filter;

import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallvanPostStatusFilter {
    ALL(null),
    RECRUITING(CallvanStatus.RECRUITING),
    CLOSED(CallvanStatus.CLOSED),
    COMPLETED(CallvanStatus.COMPLETED);

    private final CallvanStatus status;
}
