package in.koreatech.koin.domain.bus.model.express;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

/**
 * OpenApi 상세: 국토교통부_전국 버스정류장 위치정보 (버스 정류장 노드 ID)
 * https://www.data.go.kr/data/15067528/fileData.do
 */
@Getter
public enum ExpressBusStationNode {
    KOREATECH("NAI3125301"), // 코리아텍
    TERMINAL("NAI3112001"), // 종합터미널
    ;

    private final String stationId;

    ExpressBusStationNode(String stationId) {
        this.stationId = stationId;
    }

    public static ExpressBusStationNode from(String stationName) {
        return Arrays.stream(values()).
            filter(it -> it.name().equalsIgnoreCase(stationName))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("stationName: " + stationName));
    }
}
