package in.koreatech.koin.batch.campus.bus.city.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
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
    private TimetableDocument(
        String routeId,
        List<CityBusTimetable> busTimetables,
        BusInfo busInfo,
        LocalDateTime updatedAt) {
        this.routeId = routeId;
        this.busTimetables = busTimetables;
        this.busInfo = busInfo;
        this.updatedAt = updatedAt;
    }
}
