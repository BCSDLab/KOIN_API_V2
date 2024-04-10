package in.koreatech.koin.domain.bus.model.express;

import java.time.format.DateTimeFormatter;

import in.koreatech.koin.domain.bus.model.BusTimetable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExpressBusTimetable extends BusTimetable {

    private final String depart;

    private final String arrival;

    private final int charge;

    public ExpressBusTimetable(String depart, String arrival, int charge){
        this.depart = depart;
        this.arrival = arrival;
        this.charge = charge;
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ExpressBusTimetable from(ExpressBusCacheInfo expressBusCacheInfo){
        String departure = expressBusCacheInfo.depart().format(TIME_FORMATTER);
        String arrival = expressBusCacheInfo.arrival().format(TIME_FORMATTER);
        int charge = expressBusCacheInfo.charge();
        return new ExpressBusTimetable(departure, arrival, charge);
    }
}
