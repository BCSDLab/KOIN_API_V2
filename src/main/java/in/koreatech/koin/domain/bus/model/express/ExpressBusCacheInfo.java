package in.koreatech.koin.domain.bus.model.express;

import java.time.LocalTime;

public record ExpressBusCacheInfo(
    LocalTime departureTime,
    LocalTime arrivalTime,
    int charge
) {

}
