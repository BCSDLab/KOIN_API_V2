package in.koreatech.koin.common.event;

import java.util.List;

/** 이번 처리에서 승인으로 전이된 제보자만 담는다. */
public record DiningReportApprovedEvent(Integer diningId, List<Integer> reporterIds) {
    public DiningReportApprovedEvent {
        reporterIds = List.copyOf(reporterIds);
    }
}
