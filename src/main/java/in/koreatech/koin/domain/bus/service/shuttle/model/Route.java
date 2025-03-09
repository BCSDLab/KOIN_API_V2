package in.koreatech.koin.domain.bus.service.shuttle.model;

import static in.koreatech.koin.domain.bus.enums.ShuttleRouteType.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.exception.BusArrivalNodeNotFoundException;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
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

    private String direction;

    public boolean isRunning(Clock clock) {
        if ("미운행".equals(routeName) || arrivalNodes.isEmpty()) {
            return false;
        }
        String todayOfWeek = LocalDateTime.now(clock)
            .getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, Locale.US)
            .toUpperCase();
        return runningDays.contains(todayOfWeek);
    }

    public boolean isCorrectRoute(BusStation depart, BusStation arrival, Clock clock) {
        boolean foundDepart = false;
        for (ArrivalNode node : arrivalNodes) {
            if (depart.getDisplayNames().contains(node.getNodeName())
                && (BusRemainTime.from(node.getArrivalTime()).isBefore(clock))) {
                foundDepart = true;
            }
            if (arrival.getDisplayNames().contains(node.getNodeName()) && foundDepart) {
                return true;
            }
        }
        return false;
    }

    public BusRemainTime getRemainTime(BusStation busStation) {
        ArrivalNode convertedNode = convertToArrivalNode(busStation);
        return BusRemainTime.from(convertedNode.getArrivalTime());
    }

    private ArrivalNode convertToArrivalNode(BusStation busStation) {
        return arrivalNodes.stream()
            .filter(node -> busStation.getDisplayNames().contains(node.getNodeName()))
            .findFirst()
            .orElseThrow(() -> BusArrivalNodeNotFoundException.withDetail(
                "routeName: " + routeName + ", busStation: " + busStation.name()));
    }

    public void sortArrivalNodesByDirection() {
        setDirection();
        if (direction.equals("하교") && routeType != SHUTTLE) {
            Collections.reverse(this.arrivalNodes);
        }
    }

    private void setDirection() {
        if (routeType.equals(WEEKDAYS)) {
            this.direction = routeInfo;
            return;
        }
        if (routeType.equals(WEEKEND)) {
            this.direction = routeDetail;
            return;
        }
        this.direction = arrivalNodes.get(0).getArrivalTime() == null ? "등교" : "하교";
    }
}
