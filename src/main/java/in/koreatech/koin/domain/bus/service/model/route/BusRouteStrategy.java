package in.koreatech.koin.domain.bus.service.model.route;

import java.util.List;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusRouteType;

public interface BusRouteStrategy {

    List<ScheduleInfo> findSchedule(BusRouteCommand command);
    boolean support(BusRouteType type);
}
