package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class RouteInfo {

    private String name;
    private String detail;
    private List<String> runningDays;
    private List<String> arrivalTime;

    public static RouteInfo from(InnerNameDetail innerNameDetail, RunningDays runningDays, ArrivalTime arrivalTime) {
        return RouteInfo.builder()
            .name(innerNameDetail.name)
            .detail(innerNameDetail.detail)
            .runningDays(runningDays.getDays())
            .arrivalTime(arrivalTime.getTimes())
            .build();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class InnerNameDetail {

        private String name;
        private String detail;

        public static InnerNameDetail of(String name, String detail) {
            return new InnerNameDetail(name, detail);
        }
    }
}
