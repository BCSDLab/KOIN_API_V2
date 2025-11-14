package in.koreatech.koin.admin.bus.commuting.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class RouteInfo {
    @Getter
    private final String name;

    @Getter
    private final String detail;

    private final List<ArrivalTime> arrivalTimes;

    private RouteInfo(String name) {
        this.name = name;
        this.detail = null;
        this.arrivalTimes = new ArrayList<>();
    }

    public static RouteInfo from(String name) {
        return new RouteInfo(name);
    }

    public void addArrivalTime(ArrivalTime arrivalTime) {
        arrivalTimes.add(arrivalTime);
    }

    public List<String> getArrivalTimesAsStringList() {
        return arrivalTimes.stream().map(ArrivalTime::time).toList();
    }
}
