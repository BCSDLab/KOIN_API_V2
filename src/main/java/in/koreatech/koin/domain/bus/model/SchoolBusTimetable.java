package in.koreatech.koin.domain.bus.model;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SchoolBusTimetable extends BusTimetable {
    private final String routeName;
    private final List<ArrivalNode> arrivalInfo;

    public SchoolBusTimetable(String routeName, List<ArrivalNode> arrivalInfo){
        this.routeName = routeName;
        this.arrivalInfo = arrivalInfo;
    }

    @Getter
    public static class ArrivalNode implements Comparable<ArrivalNode>{
        private final String nodeName;
        private final String arrivalTime;

        public ArrivalNode(String nodeName, String arrivalTime){
            this.nodeName = nodeName;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public int compareTo(ArrivalNode o) {
            return arrivalTime.compareTo(o.arrivalTime);
        }
    }
}
