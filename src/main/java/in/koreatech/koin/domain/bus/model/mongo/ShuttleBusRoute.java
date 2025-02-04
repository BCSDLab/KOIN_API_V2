package in.koreatech.koin.domain.bus.model.mongo;

import static lombok.AccessLevel.PROTECTED;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.exception.BusArrivalNodeNotFoundException;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.model.enums.ShuttleRouteType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Document(collection = "shuttlebus_timetables")
public class ShuttleBusRoute {

    @Id
    private String id;

    @Field("semester_type")
    private String semesterType;

    @Field("region")
    private ShuttleBusRegion region;

    @Field("route_type")
    private ShuttleRouteType routeType;

    @Field("route_name")
    private String routeName;

    @Field("sub_name")
    private String subName;

    @Field("node_info")
    private List<NodeInfo> nodeInfo;

    @Field("route_info")
    private List<RouteInfo> routeInfo;

    @Getter
    @NoArgsConstructor(access = PROTECTED)
    public static class NodeInfo {

        @Field("name")
        private String name;

        @Field("detail")
        private String detail;
    }

    @Getter
    @NoArgsConstructor(access = PROTECTED)
    public static class RouteInfo {

        @Field("name")
        private String name;

        @Field("detail")
        private String detail;

        @Field("running_days")
        private List<String> runningDays;

        @Field("arrival_time")
        private List<String> arrivalTime;

        public boolean filterRoutesByDayOfWeek(LocalDate date) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return runningDays.contains(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase());
        }
    }

    public boolean filterDepartAndArriveNode(BusStation departNode, BusStation arriveNode) {
        boolean foundDepart = false;
        for (NodeInfo node : nodeInfo) {
            for (String nodeName : departNode.getDisplayNames()) {
                if (!foundDepart && node.getName().contains(nodeName)) {
                    foundDepart = true;
                    break;
                }
            }
            for (String nodeName : arriveNode.getDisplayNames()) {
                if (foundDepart && node.getName().contains(nodeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int findArrivalNodeIndexByStation(BusStation departNode) {
        for (int i = 0; i < nodeInfo.size(); i++) {
            for (String nodeName : departNode.getDisplayNames()) {
                if (nodeInfo.get(i).getName().contains(nodeName)) {
                    return i;
                }
            }
        }
        throw new BusArrivalNodeNotFoundException("");
    }
}
