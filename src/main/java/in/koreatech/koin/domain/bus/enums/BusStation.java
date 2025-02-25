package in.koreatech.koin.domain.bus.enums;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusStation {
    KOREATECH(List.of("학교", "한기대", "코리아텍", "대학", "본교"), BusStationNode.KOREATECH, "한기대"),
    STATION(List.of("천안역", "천안역(학화호두과자)"), BusStationNode.STATION, "천안역"),
    TERMINAL(List.of("터미널", "터미널(신세계 앞 횡단보도)", "야우리", "천안 터미널"), BusStationNode.TERMINAL, "터미널"),
    ;

    private final List<String> displayNames;
    private final BusStationNode node;
    private final String queryName;

    BusStation(List<String> displayNames, BusStationNode node, String queryName) {
        this.displayNames = displayNames;
        this.node = node;
        this.queryName = queryName;
    }

    @JsonCreator
    public static BusStation from(String busStationName) {
        return Arrays.stream(values())
            .filter(
                busStation -> busStation.name().equalsIgnoreCase(busStationName) ||
                    busStation.displayNames.contains(busStationName)
            )
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busStation: " + busStationName));
    }

    public static BusDirection getDirection(BusStation depart, BusStation arrival) {
        if (depart.ordinal() < arrival.ordinal()) {
            return BusDirection.SOUTH;
        }
        return BusDirection.NORTH;
    }

    public List<String> getNodeId(BusDirection direction) {
        return node.getId(direction);
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
