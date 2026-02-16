package in.koreatech.koin.domain.callvan.model.filter;

import java.util.List;

import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallvanPostStatusFilter {
    RECRUITING(CallvanStatus.RECRUITING),
    CLOSED(CallvanStatus.CLOSED),
    COMPLETED(CallvanStatus.COMPLETED);

    private final CallvanStatus status;

    public static List<CallvanStatus> toStatuses(List<CallvanPostStatusFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        return filters.stream()
            .map(CallvanPostStatusFilter::getStatus)
            .distinct()
            .toList();
    }
}
