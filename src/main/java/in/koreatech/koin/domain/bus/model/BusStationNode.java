package in.koreatech.koin.domain.bus.model;

import static in.koreatech.koin.domain.bus.model.BusDirection.NORTH;
import static in.koreatech.koin.domain.bus.model.BusDirection.SOUTH;

import java.util.Map;

/**
 * OpenApi 상세: 국토교통부_전국 버스정류장 위치정보 (버스 정류장 노드 ID)
 * https://www.data.go.kr/data/15067528/fileData.do
 */
public enum BusStationNode {
    TERMINAL(Map.of(NORTH, "CAB285000686", SOUTH, "CAB285000685")), // 종합터미널
    KOREATECH(Map.of(NORTH, "CAB285000406", SOUTH, "CAB285000405")), // 코리아텍
    STATION(Map.of(NORTH, "CAB285000655", SOUTH, "CAB285000656")), // 천안역 동부광장
    ;

    private final Map<BusDirection, String> node;

    BusStationNode(Map<BusDirection, String> node) {
        this.node = node;
    }

    public String getId(BusDirection direction) {
        return node.get(direction);
    }
}
