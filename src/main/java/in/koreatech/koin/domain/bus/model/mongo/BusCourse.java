package in.koreatech.koin.domain.bus.model.mongo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Document(collection = "bus_timetables")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusCourse {

    @Id
    @Field("_id")
    private String id;

    @Field("bus_type")
    private String busType;

    @Field("region")
    private String region;

    @Field("direction")
    private String direction;

    @Field("routes")
    private List<Route> routes = new ArrayList<>();

    @Builder
    private BusCourse(String busType, String region, String direction, List<Route> routes) {
        this.busType = busType;
        this.region = region;
        this.direction = direction;
        this.routes = routes;
    }
}
