package in.koreatech.koin.domain.bus.model;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;

@Getter
public class Route {

    @Field("route_name")
    private String routeName;

    @Field("running_days")
    private List<String> runningDays;

    @Field("arrival_info")
    private List<ArrivalNode> arrivalInfos;

    public boolean isRunning() {
        if (routeName.equals("미운행") || arrivalInfos.isEmpty()) {
            return false;
        }
        String todayOfWeek = LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();
        return runningDays.contains(todayOfWeek);
    }

    @Getter
    public static class ArrivalNode {

        @Field("node_name")
        private String nodeName;

        @Field("arrival_time")
        private String arrivalTime;
    }
}
