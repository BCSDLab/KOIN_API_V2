package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusStation {
    KOREATECH(List.of("학교", "한기대", "코리아텍"), BusStationNode.KOREATECH),
    STATION(List.of("천안역", "천안역(학화호두과자)"), BusStationNode.STATION),
    TERMINAL(List.of("터미널", "터미널(신세계 앞 횡단보도)", "야우리"), BusStationNode.TERMINAL),
    ;

    private final List<String> displayNames;
    private final BusStationNode node;

    BusStation(List<String> displayNames, BusStationNode node) {
        this.displayNames = displayNames;
        this.node = node;
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
