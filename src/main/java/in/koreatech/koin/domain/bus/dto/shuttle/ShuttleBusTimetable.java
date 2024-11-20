package in.koreatech.koin.domain.bus.dto.shuttle;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.dto.BusTimetable;
import lombok.Getter;

@Getter
@JsonNaming(value = SnakeCaseStrategy.class)
public class ShuttleBusTimetable extends BusTimetable {

    private final String routeName;
    private final List<ArrivalNode> arrivalInfo;

    public ShuttleBusTimetable(String routeName, List<ArrivalNode> arrivalInfo) {
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
