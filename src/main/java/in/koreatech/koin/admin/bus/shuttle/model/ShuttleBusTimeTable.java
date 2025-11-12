package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Document(collection = "shuttlebus-timetables")
public class ShuttleBusTimeTable {

    @Id
    private String id;

    @Field(name = "semester_type")
    private String semesterType;

    @Field(name = "region")
    private Region region;

    @Field(name = "routeType")
    private String routeType;

    @Field(name = "routeName")
    private String routeName;

    @Field(name = "subName")
    private String subName;

    @Field(name = "node_info")
    private List<NodeInfo> nodeInfos;

    @Field(name = "route_info")
    private List<RouteInfo> routeInfos;

    public static ShuttleBusTimeTable from(
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos,
        Region region,
        RouteName routeName,
        SubName subName,
        RouteType routeType
    ) {
        return ShuttleBusTimeTable.builder()
            .nodeInfos(nodeInfos)
            .routeInfos(routeInfos)
            .region(region)
            .routeName(routeName.getName())
            .subName(subName.getName())
            .routeType(routeType.getValue())
            .build();
    }
}
