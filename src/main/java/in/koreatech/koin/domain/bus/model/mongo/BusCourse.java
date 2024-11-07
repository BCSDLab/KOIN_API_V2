package in.koreatech.koin.domain.bus.model.mongo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public List<Route> getBusRoutesByDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return routes.stream().filter(route -> route.getRunningDays()
                .contains(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase()))
                .toList();
    }
}
