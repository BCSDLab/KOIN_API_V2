package in.koreatech.koin.admin.bus.commuting.model;

import java.util.ArrayList;
import java.util.List;

public class RouteInfo {
    private String name;
    private String detail;
    private List<ArrivalTime> arrivalTimes;

    public RouteInfo(String name) {
        this.name = name;
        this.detail = null;
        this.arrivalTimes = new ArrayList<>();
    }

    public void addArrivalTime(ArrivalTime arrivalTime) {
        arrivalTimes.add(arrivalTime);
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public List<ArrivalTime> getArrivalTimes() {
        return List.copyOf(arrivalTimes);
    }

    public List<String> getArrivalTimesAsStringList() {
        return arrivalTimes.stream().map(ArrivalTime::time).toList();
    }
}
