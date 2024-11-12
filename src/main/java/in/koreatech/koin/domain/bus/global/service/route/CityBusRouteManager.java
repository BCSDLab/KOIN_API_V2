package in.koreatech.koin.domain.bus.global.service.route;

import static in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo.toScheduleInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.bus.city.model.CityBusTimetable.BusTimetable;
import in.koreatech.koin.domain.bus.global.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import in.koreatech.koin.domain.bus.city.model.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.city.model.CityBusTimetable;

public class CityBusRouteManager {

    private static final String BUS_TYPE = "city";

    public static List<ScheduleInfo> getCityBusSchedule(
        CityBusTimetable timetable,
        Long busNumber,
        BusStation depart,
        CityBusDirection arrival,
        LocalDate date
    ) {
        Optional<BusTimetable> optionalBusTimetable = timetable.getBusTimetableByDate(date);

        if (optionalBusTimetable.isPresent()) {
            List<LocalTime> departTimes = optionalBusTimetable.get().adjustDepartTimes(busNumber, arrival, depart);
            return toScheduleInfo(departTimes, BUS_TYPE, busNumber.toString());
        } else {
            return null;
        }
    }

}
