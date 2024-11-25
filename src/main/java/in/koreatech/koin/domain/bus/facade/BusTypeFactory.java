package in.koreatech.koin.domain.bus.facade;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BusTypeFactory {
    private final Set<BusTypeInterface> busTypeInterfaces;

    public List<? extends BusRemainTime> getBusRemainTime(BusType busType, BusStation depart, BusStation arrival) {
        return busTypeInterfaces.stream()
            .filter(busTypeInterface -> busTypeInterface.support(busType))
            .findFirst()
            .orElseThrow(() -> new BusTypeNotFoundException(busType.getName()))
            .getBusRemainTime(busType, depart, arrival);
    }

}
