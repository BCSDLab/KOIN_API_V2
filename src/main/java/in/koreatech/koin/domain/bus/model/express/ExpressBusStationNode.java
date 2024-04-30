package in.koreatech.koin.domain.bus.model.express;

import java.util.Arrays;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import lombok.Getter;

/**
 * OpenApi 상세: 국토교통부_전국 버스정류장 위치정보 (버스 정류장 노드 ID)
 * https://www.data.go.kr/data/15067528/fileData.do
 */
@Getter
public enum ExpressBusStationNode {
    KOREATECH(BusStation.KOREATECH, "NAI3125301"), // 코리아텍
    TERMINAL(BusStation.TERMINAL, "NAI3112001"), // 종합터미널
    ;

    private final BusStation busStation;
    private final String stationId;

    ExpressBusStationNode(BusStation busStation, String stationId) {
        this.busStation = busStation;
        this.stationId = stationId;
    }

    public static ExpressBusStationNode from(BusStation busStation) {
        return Arrays.stream(values())
            .filter(it -> it.busStation.equals(busStation))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busStation: " + busStation));
    }
}
