package in.koreatech.koin.domain.bus.model;

import lombok.Builder;

@Builder
public record CityBusArrivalInfo(
    Long arrprevstationcnt, // 남은 정거장 개수, 5
    Long arrtime, // 도착까지 남은 시간 [초], 222
    String nodeid, // 정류소 id, "CAB285000405"
    String nodenm, // 정류소명, "코리아텍"
    String routeid, // 노선 id, "CAB285000147"
    Long routeno, // 버스 번호, 402
    String routetp, // 노선 유형, "일반 버스"
    String vehicletp // 차량 유형 (저상버스), "일반차량"
) {
    public static CityBusArrivalInfo getEmpty(String nodeid) {
        return builder()
            .nodeid(nodeid)
            .arrtime(-1L)
            .build();
    }
}
