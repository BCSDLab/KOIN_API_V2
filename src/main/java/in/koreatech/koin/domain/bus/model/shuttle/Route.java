package in.koreatech.koin.domain.bus.model.shuttle;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.exception.BusArrivalNodeNotFoundException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Field("route_name")
    private String routeName;

    @Field("running_days")
    private List<String> runningDays = new ArrayList<>();

    @Field("arrival_info")
    private List<ArrivalNode> arrivalInfos = new ArrayList<>();

    @Builder
    private Route(String routeName, List<String> runningDays, List<ArrivalNode> arrivalInfos) {
        this.routeName = routeName;
        this.runningDays = runningDays;
        this.arrivalInfos = arrivalInfos;
    }

    public boolean isRunning(Clock clock) {
        if ("미운행".equals(routeName) || arrivalInfos.isEmpty()) {
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
        for (ArrivalNode node : arrivalInfos) {
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
        return BusRemainTime.from(convertedNode.arrivalTime);
    }

    private ArrivalNode convertToArrivalNode(BusStation busStation) {
        return arrivalInfos.stream()
            .filter(node -> busStation.getDisplayNames().contains(node.getNodeName()))
            .findFirst()
            .orElseThrow(() -> BusArrivalNodeNotFoundException.withDetail(
                "routeName: " + routeName + ", busStation: " + busStation.name()));
    }

    public ArrivalNode checkContainNode(String name) {
        return arrivalInfos.stream()
            .filter(node -> node.getNodeName().contains(name))
            .findFirst()
            .orElse(null);
    }

    public List<ArrivalNode> checkContainNodes(String name) {
        return arrivalInfos.stream()
            .filter(node -> node.getNodeName().contains(name))
            .toList();
    }

    @Getter
    public static class ArrivalNode {

        @Field("node_name")
        private final String nodeName;

        @Field("arrival_time")
        private final String arrivalTime;

        @Builder
        private ArrivalNode(String nodeName, String arrivalTime) {
            this.nodeName = nodeName;
            this.arrivalTime = arrivalTime;
        }

    }
}
