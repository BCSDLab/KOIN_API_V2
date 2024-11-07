package in.koreatech.koin.domain.bus.service.route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.model.mongo.CityBusTimetable;

public class CityBusRouteManager {

    private static final String BUS_TYPE = "city";

    public static List<ScheduleInfo> getCityBusSchedule(CityBusTimetable timetable, Long busNumber, BusStation depart, CityBusDirection arrival, LocalDate date) {
        Optional<CityBusTimetable.BusTimetable> optionalBusTimetable = timetable.getBusTimetableByDate(date);

        if (optionalBusTimetable.isPresent()) {
            List<LocalTime> departTimes = optionalBusTimetable.get().adjustDepartTimes(busNumber, arrival, depart);
            return ScheduleInfo.toScheduleInfo(departTimes, BUS_TYPE, busNumber.toString());
        } else {
            return null;
        }
    }

}
