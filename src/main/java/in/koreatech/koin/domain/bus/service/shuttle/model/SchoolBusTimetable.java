package in.koreatech.koin.domain.bus.service.shuttle.model;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.service.model.BusTimetable;
import lombok.Getter;

@Getter
@JsonNaming(value = SnakeCaseStrategy.class)
public class SchoolBusTimetable extends BusTimetable {
    private final String routeName;
    private final List<ArrivalNode> arrivalInfo;

    public SchoolBusTimetable(String routeName, List<ArrivalNode> arrivalInfo) {
        this.routeName = routeName;
        this.arrivalInfo = arrivalInfo;
    }
}
