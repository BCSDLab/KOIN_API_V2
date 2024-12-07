package in.koreatech.koin.domain.bus.model.express;

import java.time.LocalTime;
import java.util.List;

public final class ExpressBusSchedule {

    private static final List<LocalTime> KOREA_TECH_SCHEDULE = List.of(
        LocalTime.of(7, 0),
        LocalTime.of(8, 30),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        LocalTime.of(12, 0),
        LocalTime.of(12, 30),
        LocalTime.of(13, 0),
        LocalTime.of(15, 0),
        LocalTime.of(16, 0),
        LocalTime.of(16, 40),
        LocalTime.of(18, 0),
        LocalTime.of(19, 30),
        LocalTime.of(20, 30)
    );

    private static final List<LocalTime> TERMINAL_SCHEDULE = List.of(
        LocalTime.of(8, 35),
        LocalTime.of(10, 35),
        LocalTime.of(11, 5),
        LocalTime.of(11, 35),
        LocalTime.of(13, 35),
        LocalTime.of(14, 35),
        LocalTime.of(15, 5),
        LocalTime.of(16, 35),
        LocalTime.of(17, 35),
        LocalTime.of(19, 5),
        LocalTime.of(19, 35),
        LocalTime.of(21, 5),
        LocalTime.of(22, 5)
    );

    public static List<LocalTime> getExpressBusScheduleToKoreaTech() {
        return KOREA_TECH_SCHEDULE;
    }

    public static List<LocalTime> getExpressBusScheduleToTerminal() {
        return TERMINAL_SCHEDULE;
    }
}
