package in.koreatech.koin.domain.bus.model.express;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusTimetable;
import lombok.Getter;

@Getter
@JsonNaming(value = SnakeCaseStrategy.class)
public class ExpressBusTimetable extends BusTimetable {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @JsonProperty(value = "depart")
    private final String depart;

    private final String arrival;

    private final int charge;

    public ExpressBusTimetable(String depart, String arrival, int charge){
        this.depart = depart;
        this.arrival = arrival;
        this.charge = charge;
    }

    public static ExpressBusTimetable from(ExpressBusCacheInfo expressBusCacheInfo){
        String departure = expressBusCacheInfo.depart().format(TIME_FORMATTER);
        String arrival = expressBusCacheInfo.arrival().format(TIME_FORMATTER);
        int charge = expressBusCacheInfo.charge();
        return new ExpressBusTimetable(departure, arrival, charge);
    }
}
