package in.koreatech.koin.domain.bus.service.shuttle.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = PROTECTED)
@Document(collection = "shuttlebus_timetables")
public class ShuttleBusSimpleRoute {

    @Field("route_name")
    private String routeName;

    @Field("route_type")
    private ShuttleRouteType routeType;

    @Field("region")
    private ShuttleBusRegion region;

    @Field("route_info_name")
    private String routeInfo;

    @Field("route_detail")
    private String routeDetail;

    @Field("array_lengths_match")
    private boolean arrayLengthsMatch = true;

    @Field("node_name")
    private List<String> nodeName;

    @Field("arrival_time")
    private List<String> arrivalTime;
}
