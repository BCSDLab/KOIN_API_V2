package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunningDays {

    private List<String> days;

    public List<String> getDays() {
        return days;
    }

    public static RunningDays weekDays() {
        return new RunningDays(List.of("MON", "TUE", "WED", "THU", "FRI"));
    }

    public static RunningDays thursdayAndFriday() {
        return new RunningDays(List.of("THU", "FRI"));
    }

    public static RunningDays saturday() {
        return new RunningDays(List.of("SAT"));
    }

    public static RunningDays sunday() {
        return new RunningDays(List.of("SUN"));
    }
}
