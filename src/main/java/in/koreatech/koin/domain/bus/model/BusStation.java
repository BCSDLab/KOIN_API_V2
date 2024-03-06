package in.koreatech.koin.domain.bus.model;

<<<<<<< HEAD
=======
import java.util.Arrays;
>>>>>>> feature/99-get-bus-shuttle-and-commuting
import java.util.List;

import in.koreatech.koin.domain.bus.exception.BusStationNotFoundException;
import lombok.Getter;

@Getter
public enum BusStation {
    KOREATECH("koreatech", List.of("학교", "한기대"), BusStationNode.KOREATECH),
    STATION("station", List.of("천안역", "천안역(학화호두과자)"), BusStationNode.STATION),
    TERMINAL("terminal", List.of("터미널", "터미널(신세계 앞 횡단보도)"), BusStationNode.TERMINAL),
    ;

    private final String name;
    private final List<String> displayNames;
    private final BusStationNode node;

    BusStation(String name, List<String> displayNames, BusStationNode node) {
        this.name = name;
        this.displayNames = displayNames;
        this.node = node;
    }

    public static BusStation from(String busStationName) {
        return Arrays.stream(values())
            .filter(busStation -> busStation.name.equals(busStationName))
            .findAny()
            .orElseThrow(() -> BusStationNotFoundException.withDetail("busStation: " + busStationName));
    }

    public static BusDirection getDirection(BusStation depart, BusStation arrival) {
        if (depart.ordinal() < arrival.ordinal()) {
            return BusDirection.SOUTH;
        }
        return BusDirection.NORTH;
    }

    public String getNodeId(BusDirection direction) {
        return node.getId(direction);
    }
}
