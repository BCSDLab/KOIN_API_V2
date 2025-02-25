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
    private final List<ArrivalNode> arrivalNodes;

    public SchoolBusTimetable(Route route) {
        this.routeName = route.getRouteName();
        this.arrivalNodes = route.getArrivalNodes();
    }
}
