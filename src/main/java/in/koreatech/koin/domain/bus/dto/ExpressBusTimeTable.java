package in.koreatech.koin.domain.bus.dto;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalTime;

import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.OpenApiExpressBusArrival;

public record ExpressBusTimeTable(
    LocalTime depart,
    LocalTime arrival,
    int charge
) {

    public static ExpressBusTimeTable from(OpenApiExpressBusArrival openApiExpressBusArrival) {
        LocalTime departure = LocalTime.parse(openApiExpressBusArrival.depPlandTime(), ofPattern("yyyyMMddHHmm"));
        LocalTime arrival = LocalTime.parse(openApiExpressBusArrival.arrPlandTime(), ofPattern("yyyyMMddHHmm"));
        int charge = openApiExpressBusArrival.charge();
        return new ExpressBusTimeTable(departure, arrival, charge);
    }

    public static ExpressBusTimeTable from(ExpressBusCacheInfo expressBusCacheInfo) {
        LocalTime departure = expressBusCacheInfo.departureTime();
        LocalTime arrival = expressBusCacheInfo.arrivalTime();
        int charge = expressBusCacheInfo.charge();
        return new ExpressBusTimeTable(departure, arrival, charge);
    }
}
