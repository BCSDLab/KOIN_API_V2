package in.koreatech.koin.domain.bus.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Document(collection = "bus_timetables")
public class Bus {

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
    private List<Route> routes;
}
