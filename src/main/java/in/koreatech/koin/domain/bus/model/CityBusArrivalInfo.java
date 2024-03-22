package in.koreatech.koin.domain.bus.model;

import lombok.Builder;

/**
 * "arrprevstationcnt": 5,
 * "arrtime": 222,
 * "nodeid": "CAB285000405",
 * "nodenm": "코리아텍",
 * "routeid": "CAB285000147",
 * "routeno": 402,
 * "routetp": "일반버스",
 * "vehicletp": "일반차량"
 */

public record CityBusArrivalInfo(
    Long arrprevstationcnt, // 남은 정거장 개수
    Long arrtime, // 도착까지 남은 시간 [초]
    String nodeid, // 정류소 id
    String nodenm, // 정류소명
    String routeid, // 노선 id
    Long routeno, // 버스 번호
    String routetp, // 노선 유형
    String vehicletp // 차량 유형 (저상버스)
) {
    public static CityBusArrivalInfo getEmpty(String nodeid) {
        return builder()
            .nodeid(nodeid)
            .arrtime(-1L)
            .build();
    }

    @Builder
    public CityBusArrivalInfo {
    }
    /*
    @Override
    public int compareTo(CityBusArrivalInfo o) {
        return this.arrtime - o.arrtime;
    }*/
}
