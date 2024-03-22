package in.koreatech.koin.domain.bus.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CityBusArrivalInfo/* implements Comparable<CityBusArrivalInfo> */ {
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
    private Long arrprevstationcnt; // 남은 정거장 개수
    private Long arrtime; // 도착까지 남은 시간 [초]
    private String nodeid; // 정류소 id
    private String nodenm; // 정류소명
    private String routeid; // 노선 id
    private Long routeno; // 버스 번호
    private String routetp; // 노선 유형
    private String vehicletp; // 차량 유형 (저상버스)

    //TODO: record로 변환 가능한지 확인
    public static CityBusArrivalInfo getEmpty(String nodeid) {
        return builder()
            .nodeid(nodeid)
            .arrtime(-1L)
            .build();
    }

    @Builder
    private CityBusArrivalInfo(Long arrprevstationcnt, Long arrtime, String nodeid, String nodenm, String routeid,
        Long routeno, String routetp, String vehicletp) {
        this.arrprevstationcnt = arrprevstationcnt;
        this.arrtime = arrtime;
        this.nodeid = nodeid;
        this.nodenm = nodenm;
        this.routeid = routeid;
        this.routeno = routeno;
        this.routetp = routetp;
        this.vehicletp = vehicletp;
    }
    /*
    @Override
    public int compareTo(CityBusArrivalInfo o) {
        return this.arrtime - o.arrtime;
    }*/
}
