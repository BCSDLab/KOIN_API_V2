package in.koreatech.koin.domain.bus.city.model.enums;

import static in.koreatech.koin.domain.bus.city.model.enums.BusDirection.NORTH;
import static in.koreatech.koin.domain.bus.city.model.enums.BusDirection.SOUTH;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * OpenApi 상세: 국토교통부_전국 버스정류장 위치정보 (버스 정류장 노드 ID)
 * https://www.data.go.kr/data/15067528/fileData.do
 */
@Getter
public enum BusStationNode {
    TERMINAL(Map.of(NORTH, List.of("CAB285000686"), SOUTH, List.of("CAB285000685", "CAB285010125"))), // 종합터미널
    KOREATECH(Map.of(NORTH, List.of("CAB285000406"), SOUTH, List.of("CAB285000405"))), // 코리아텍
    STATION(Map.of(NORTH, List.of("CAB285000655"), SOUTH, List.of("CAB285000656"))), // 천안역 동부광장
    ;

    private final Map<BusDirection, List<String>> node;

    BusStationNode(Map<BusDirection, List<String>> node) {
        this.node = node;
    }

    public List<String> getId(BusDirection direction) {
        return node.get(direction);
    }

    public static List<String> getNodeIds() {
        return Arrays.stream(values())
            .flatMap(station -> station.node.values().stream().flatMap(List::stream))
            .toList();
    }
}
