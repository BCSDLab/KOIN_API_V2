package in.koreatech.koin.batch.campus.bus.city.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CityBusTimetable {

    @Field("day_of_week")
    private final String dayOfWeek;

    @Field("depart_info")
    private final List<String> departInfo;

    @Builder
    private CityBusTimetable(String dayOfWeek, List<String> departInfo) {
        this.dayOfWeek = dayOfWeek;
        this.departInfo = departInfo;
    }
}
