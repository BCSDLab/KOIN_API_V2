package in.koreatech.koin.batch.campus.bus.city.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import in.koreatech.koin.batch.campus.bus.city.dto.CityBusRouteInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Document(collection = "citybus_timetables")
public class TimetableDocument {

    @Id
    @Field("_id")
    private final String routeId;

    @Field("bus_info")
    private final BusInfo busInfo;

    @Field("bus_timetables")
    private final List<CityBusTimetable> busTimetables;

    @Field("updated_at")
    private final LocalDateTime updatedAt;

    @Builder
    private TimetableDocument(List<CityBusTimetable> busTimetables, CityBusRouteInfo routeInfo,
        LocalDateTime updatedAt) {
        this.routeId = routeInfo.routeId().toString();
        this.busTimetables = busTimetables;
        this.busInfo = BusInfo.builder()
            .number(Long.parseLong(routeInfo.routeName()))
            .departNode(routeInfo.stName())
            .arrivalNode(routeInfo.edName())
            .build();
        this.updatedAt = updatedAt;
    }
}
