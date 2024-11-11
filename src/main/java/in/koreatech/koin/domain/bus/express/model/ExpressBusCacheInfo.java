package in.koreatech.koin.domain.bus.express.model;

import java.time.LocalTime;

public record ExpressBusCacheInfo(
    LocalTime depart,
    LocalTime arrival,
    int charge
) {

}
