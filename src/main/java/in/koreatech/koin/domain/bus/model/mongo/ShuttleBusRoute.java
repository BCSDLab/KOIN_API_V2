package in.koreatech.koin.domain.bus.model.mongo;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Document(collection = "shuttlebus_timetables")
public class ShuttleBusRoute {

    @Id
    private String id;

    @Field("region")
    private String region;

    @Field("route_type")
    private String routeType;

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

        @Field("running_days")
        @JsonIgnore
        private List<String> runningDays;

        @Field("arrival_time")
        private List<String> arrivalTime;
    }
}
