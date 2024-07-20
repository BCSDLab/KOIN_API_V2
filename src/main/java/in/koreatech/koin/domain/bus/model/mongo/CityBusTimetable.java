package in.koreatech.koin.domain.bus.model.mongo;

import java.time.LocalDateTime;
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
@Document(collection = "citybus_timetables")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CityBusTimetable {

    @Id
    @Field("_id")
    private String routeId;

    @Field("bus_info")
    private BusInfo busInfo;

    @Field("bus_timetables")
    private List<BusTimetable> busTimetables = new ArrayList<>();

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private CityBusTimetable(BusInfo busInfo, List<BusTimetable> busTimetables, LocalDateTime updatedAt) {
        this.busInfo = busInfo;
        this.busTimetables = busTimetables;
        this.updatedAt = updatedAt;
    }

    @Getter
    public static class BusInfo {

        @Field("number")
        private Long number;

        @Field("depart_node")
        private String depart;

        @Field("arrival_node")
        private String arrival;

        @Builder
        private BusInfo(Long number, String depart, String arrival) {
            this.number = number;
            this.depart = depart;
            this.arrival = arrival;
        }
    }

    @Getter
    public static class BusTimetable {
        @Field("day_of_week")
        private String dayOfWeek;

        @Field("depart_info")
        private List<String> departInfo;

        @Builder
        private BusTimetable(String dayOfWeek, List<String> departInfo) {
            this.dayOfWeek = dayOfWeek;
            this.departInfo = departInfo;
        }
    }
}
