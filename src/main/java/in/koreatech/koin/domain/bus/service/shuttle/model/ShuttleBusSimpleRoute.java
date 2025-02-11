package in.koreatech.koin.domain.bus.service.shuttle.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("node_name")
    private List<String> nodeName;

    @Field("arrival_time")
    private List<String> arrivalTime;
}
