package in.koreatech.koin.domain.bus.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;

@Getter
public class Route {

    @Field("route_name")
    private String routeName;

    @Field("running_days")
    private List<String> runningDays;

    @Field("arrival_info")
    private List<ArrivalNode> arrivalInfo;

    public boolean isRunning() {
        if (routeName.equals("미운행")) {
            return false;
        }
        return !arrivalInfo.isEmpty();
    }

    public static class ArrivalNode {

        @Field("node_name")
        private String nodeName;

        @Field("arrival_time")
        private String arrivalTime;
    }
}
