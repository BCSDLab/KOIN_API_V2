package in.koreatech.koin.domain.bus.service.express.model;

import java.time.LocalTime;

public record ExpressBusCacheInfo(
    LocalTime depart,
    LocalTime arrival,
    int charge
) {

}
