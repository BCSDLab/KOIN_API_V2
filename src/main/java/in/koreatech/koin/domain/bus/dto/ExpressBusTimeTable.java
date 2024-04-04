package in.koreatech.koin.domain.bus.dto;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalTime;

import in.koreatech.koin.domain.bus.model.express.ExpressBusArrival;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;

public record ExpressBusTimeTable(
    LocalTime departure,
    LocalTime arrival,
    int charge
) {

    public static ExpressBusTimeTable from(ExpressBusArrival expressBusArrival) {
        LocalTime departure = LocalTime.parse(expressBusArrival.depPlandTime(), ofPattern("yyyyMMddHHmm"));
        LocalTime arrival = LocalTime.parse(expressBusArrival.arrPlandTime(), ofPattern("yyyyMMddHHmm"));
        int charge = expressBusArrival.charge();
        return new ExpressBusTimeTable(departure, arrival, charge);
    }

    public static ExpressBusTimeTable from(ExpressBusCacheInfo expressBusCacheInfo) {
        LocalTime departure = expressBusCacheInfo.departureTime();
        LocalTime arrival = expressBusCacheInfo.arrivalTime();
        int charge = expressBusCacheInfo.charge();
        return new ExpressBusTimeTable(departure, arrival, charge);
    }
}
