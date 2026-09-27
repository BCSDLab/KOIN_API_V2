package in.koreatech.koin.domain.bus.service.shuttle.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.shuttle.internal.ShuttleRoutePathSelector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Field("route_name")
    private String routeName;

    @Field("route_type")
    private ShuttleRouteType routeType;

    @Field("region")
    private ShuttleBusRegion region;

    @Field("route_info")
    private String routeInfo;

    @Field("route_detail")
    private String routeDetail;

    @Field("running_days")
    private List<String> runningDays = new ArrayList<>();

    @Field("arrival_nodes")
    private List<ArrivalNode> arrivalNodes = new ArrayList<>();

    @Field("array_lengths_match")
    private boolean arrayLengthsMatch = true;

    private String direction;

    public boolean isRunning(Clock clock) {
        if ("미운행".equals(routeName) || "미운행".equals(routeInfo)
            || arrivalNodes == null || arrivalNodes.isEmpty() || runningDays == null) {
            return false;
        }
        String todayOfWeek = LocalDateTime.now(clock)
            .getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();
        return runningDays.contains(todayOfWeek);
    }

    public boolean isCorrectRoute(BusStation depart, BusStation arrival, Clock clock) {
        return findOccurrences(depart, arrival).stream()
            .anyMatch(occurrence -> occurrence.departureTime().isAfter(LocalDateTime.now(clock).toLocalTime()));
    }

    public List<ShuttleRoutePathSelector.Occurrence> findOccurrences(BusStation depart, BusStation arrival) {
        if (!arrayLengthsMatch || arrivalNodes == null) {
            return List.of();
        }
        List<String> nodeNames = arrivalNodes.stream()
            .map(node -> node == null ? null : node.getNodeName())
            .toList();
        List<String> arrivalTimes = arrivalNodes.stream()
            .map(node -> node == null ? null : node.getArrivalTime())
            .toList();
        return ShuttleRoutePathSelector.select(
            nodeNames,
            arrivalTimes,
            routeType,
            routeDirectionForPath(),
            depart,
            arrival
        );
    }

    public BusRemainTime getRemainTime(ShuttleRoutePathSelector.Occurrence occurrence) {
        return BusRemainTime.from(occurrence.departureTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public void sortArrivalNodesByDirection() {
        setDirection();
        if (arrivalNodes == null || arrivalNodes.isEmpty()) {
            return;
        }
        if (BusDirection.SOUTH.getName().equals(direction) && routeType != ShuttleRouteType.SHUTTLE) {
            Collections.reverse(this.arrivalNodes);
        }
    }

    private void setDirection() {
        if (routeType == ShuttleRouteType.WEEKDAYS) {
            this.direction = normalizeDirection(routeInfo);
            return;
        }

        if (routeType == ShuttleRouteType.WEEKEND) {
            this.direction = normalizeDirection(routeDetail);
            return;
        }

        this.direction = arrivalNodes == null || arrivalNodes.isEmpty()
            || arrivalNodes.get(0) == null || arrivalNodes.get(0).getArrivalTime() == null
            ? BusDirection.NORTH.getName()
            : BusDirection.SOUTH.getName();
    }

    private String routeDirectionForPath() {
        if (routeType == ShuttleRouteType.WEEKDAYS) {
            return routeInfo;
        }
        if (routeType == ShuttleRouteType.WEEKEND) {
            return routeDetail;
        }
        return null;
    }

    private String normalizeDirection(String value) {
        if (BusDirection.SOUTH.getName().equals(value)) {
            return BusDirection.SOUTH.getName();
        }
        return BusDirection.NORTH.getName();
    }
}
