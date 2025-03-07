package in.koreatech.koin.batch.campus.bus.school.dto;

import java.util.List;

import lombok.Setter;

@Setter
public class Route {

    private String route_name;
    private List<String> running_days;
    private List<ArrivalInfo> arrival_info;
}
