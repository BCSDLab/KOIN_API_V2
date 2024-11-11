package in.koreatech.koin.domain.bus.shuttle.model;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.global.model.BusTimetable;
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

    @Getter
    @JsonNaming(value = SnakeCaseStrategy.class)
    public static class ArrivalNode {
        private final String nodeName;
        private final String arrivalTime;

        public ArrivalNode(String nodeName, String arrivalTime) {
            this.nodeName = nodeName;
            this.arrivalTime = arrivalTime;
        }
    }
}
