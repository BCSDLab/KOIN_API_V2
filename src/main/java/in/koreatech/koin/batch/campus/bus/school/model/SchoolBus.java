package in.koreatech.koin.batch.campus.bus.school.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.batch.campus.bus.school.dto.Route;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Document(collection = "bus_timetables")
@JsonNaming(value = SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchoolBus {

    @Id
    @Field("_id")
    private String id;

    @Field("region")
    private String region;

    @Field("bus_type")
    private String busType;

    @Field("direction")
    private String direction;

    @Field("routes")
    private List<Route> routes;

    @Builder
    public SchoolBus(String id, String region, String busType, String direction, List<Route> routes) {
        this.id = id;
        this.region = region;
        this.busType = busType;
        this.direction = direction;
        this.routes = routes;
    }
}
