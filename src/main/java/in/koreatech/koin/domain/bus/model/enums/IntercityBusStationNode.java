package in.koreatech.koin.domain.bus.model.enums;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.bus.model.IntercityBusRoute;
import lombok.Getter;

/**
 * OpenApi 상세: 국토교통부_전국 버스정류장 위치정보 (버스 정류장 노드 ID)
 * https://www.data.go.kr/data/15067528/fileData.do
 */
@Getter
public enum IntercityBusStationNode {
    KOREATECH("NAI3125301"), // 코리아텍
    TERMINAL("NAI3112001"), // 종합터미널
    ;

    private String stationId;

    IntercityBusStationNode(String stationId) {
        this.stationId = stationId;
    }

    public static String getId(String stationName){
        return valueOf(stationName).getStationId();
    }

    public static List<IntercityBusRoute> getIds() {
        List<IntercityBusRoute> routeList = new ArrayList<>();
        routeList.add(new IntercityBusRoute(KOREATECH.getStationId(), TERMINAL.getStationId()));
        routeList.add(new IntercityBusRoute(TERMINAL.getStationId(), KOREATECH.getStationId()));
        return routeList;
    }
}