package in.koreatech.koin.batch.campus.bus.school.model;

import lombok.Getter;

@Getter
public class BusInfo {
    private final String region;
    private final BusType busType;

    public BusInfo(String region, BusType busType) {
        this.region = region;
        this.busType = busType;
    }
}
