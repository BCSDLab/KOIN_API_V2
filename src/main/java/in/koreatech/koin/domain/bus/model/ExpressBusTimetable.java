package in.koreatech.koin.domain.bus.model;

import java.time.format.DateTimeFormatter;

import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExpressBusTimetable extends BusTimetable{

    private final String departure;

    private final String arrival;

    private final int charge;

    public ExpressBusTimetable(String departure, String arrival, int charge){
        this.departure = departure;
        this.arrival = arrival;
        this.charge = charge;
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ExpressBusTimetable from(ExpressBusCacheInfo expressBusCacheInfo){
        String departure = expressBusCacheInfo.departureTime().format(TIME_FORMATTER);
        String arrival = expressBusCacheInfo.arrivalTime().format(TIME_FORMATTER);
        int charge = expressBusCacheInfo.charge();
        return new ExpressBusTimetable(departure, arrival, charge);
    }
}
