package in.koreatech.koin.domain.bus.model;

import lombok.Getter;

@Getter
public class CityBusArrivalInfo/* implements Comparable<CityBusArrivalInfo> */{
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
    private int arrprevstationcnt; // 남은 정거장 개수
    private int arrtime; // 도착까지 남은 시간
    private String nodeid;
    private String nodenm;
    private String routeid;
    private int routeno; // 버스 번호
    private String routetp;
    private String vehicletp;
/*
    @Override
    public int compareTo(CityBusArrivalInfo o) {
        return this.arrtime - o.arrtime;
    }*/
}
