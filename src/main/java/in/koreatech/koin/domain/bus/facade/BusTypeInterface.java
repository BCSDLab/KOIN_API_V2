package in.koreatech.koin.domain.bus.facade;

import java.util.List;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;

public interface BusTypeInterface {

    boolean support(BusType busType);
    List<? extends BusRemainTime> getBusRemainTime(BusType busType, BusStation depart, BusStation arrival);
}
