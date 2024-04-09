package in.koreatech.koin.domain.bus.model;

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
}
