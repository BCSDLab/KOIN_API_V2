package in.koreatech.koin.domain.bus.model.express;

import java.time.LocalTime;

public record ExpressBusCacheInfo(
    LocalTime depart,
    LocalTime arrival,
    int charge
) {

}
