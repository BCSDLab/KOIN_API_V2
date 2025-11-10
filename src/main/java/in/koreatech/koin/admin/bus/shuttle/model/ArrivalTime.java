package in.koreatech.koin.admin.bus.shuttle.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ArrivalTime {

    private List<String> times;

    public static ArrivalTime of(List<String> times) {
        return new ArrivalTime(times);
    }
}
