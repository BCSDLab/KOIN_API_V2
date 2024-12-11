package in.koreatech.koin.domain.bus.model.mongo;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
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

    public boolean filterRoutesByDayOfWeek(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return runningDays.contains(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase());
    }

    public boolean filterDepartAndArriveNode(BusStation departNode, BusStation arriveNode) {
        boolean foundDepart = false;

        for (ArrivalNode node : arrivalInfos) {
            if (!foundDepart && node.getNodeName().contains(departNode.getQueryName())
                && isValidTimeFormat(node.getArrivalTime())) {
                foundDepart = true;
            } else if (foundDepart && node.getNodeName().contains(arriveNode.getQueryName())) {
                return true;
            }
        }

        return false;
    }

    public ScheduleInfo getShuttleBusScheduleInfo(BusStation depart) {
        ArrivalNode findDepartNode = findArrivalNodeByStation(depart);
        return new ScheduleInfo("shuttle", routeName, LocalTime.parse(findDepartNode.getArrivalTime()));
    }

    public ScheduleInfo getCommutingShuttleBusScheduleInfo(BusStation depart) {
        String busType = "한기대".equals(depart.getQueryName()) ? "하교셔틀" : "등교셔틀";
        ArrivalNode findDepartNode = findArrivalNodeByStation(depart);

        return new ScheduleInfo("shuttle", String.format("%s %s", routeName, busType),
            LocalTime.parse(findDepartNode.getArrivalTime()));
    }

    private ArrivalNode findArrivalNodeByStation(BusStation depart) {
        return arrivalInfos.stream()
            .filter(arrivalNode -> arrivalNode.getNodeName().contains(depart.getQueryName()))
            .findFirst()
            .orElseThrow(() -> new BusArrivalNodeNotFoundException(""));
    }

    private boolean isValidTimeFormat(String time) {
        // HH:mm 형식의 정규식 (00:00부터 23:59까지 유효)
        String timeRegex = "([01]\\d|2[0-3]):[0-5]\\d";
        return time != null && time.matches(timeRegex);
    }

    @Builder
    private Route(String routeName, List<String> runningDays, List<ArrivalNode> arrivalInfos) {
        this.routeName = routeName;
        this.runningDays = runningDays;
        this.arrivalInfos = arrivalInfos;
    }

    @Getter
    public static class ArrivalNode {

        @Field("node_name")
        private String nodeName;

        @Field("arrival_time")
        private String arrivalTime;

        @Builder
        private ArrivalNode(String nodeName, String arrivalTime) {
            this.nodeName = nodeName;
            this.arrivalTime = arrivalTime;
        }
    }
}
