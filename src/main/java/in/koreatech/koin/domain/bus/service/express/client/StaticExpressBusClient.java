package in.koreatech.koin.domain.bus.service.express.client;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusRoute;
import in.koreatech.koin.domain.bus.service.express.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin._common.apiloadbalancer.ApiLoadBalance;

@Component
@ApiLoadBalance(ratio = 0)
public class StaticExpressBusClient extends ExpressBusClient {

    public StaticExpressBusClient(
        ExpressBusCacheRepository expressBusCacheRepository,
        VersionRepository versionRepository,
        Clock clock
    ) {
        super(expressBusCacheRepository, versionRepository, clock);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeRemainTime() {
        for (BusStation depart : BusStation.values()) {
            for (BusStation arrival : BusStation.values()) {
                if (depart == arrival || depart.equals(BusStation.STATION) || arrival.equals(BusStation.STATION)) {
                    continue;
                }
                ExpressBusCache expressBusCache = ExpressBusCache.of(
                    new ExpressBusRoute(depart.getName(), arrival.getName()),
                    getStaticResponse(depart, arrival)
                );
                if (!expressBusCache.getBusInfos().isEmpty()) {
                    expressBusCacheRepository.save(expressBusCache);
                }
            }
        }
        versionRepository.getByType(VersionType.EXPRESS).update(clock);
    }

    private List<ExpressBusCacheInfo> getStaticResponse(BusStation depart, BusStation arrival) {
        if (depart.equals(BusStation.KOREATECH) && arrival.equals(BusStation.TERMINAL)) {
            return getKoreatechToTerminal();
        }
        return getTerminalToKoreatech();
    }

    private List<ExpressBusCacheInfo> getKoreatechToTerminal() {
        return List.of(
            new ExpressBusCacheInfo(LocalTime.of(8, 35), LocalTime.of(8, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(10, 35), LocalTime.of(10, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(11, 35), LocalTime.of(11, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(13, 35), LocalTime.of(13, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(14, 35), LocalTime.of(14, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(16, 35), LocalTime.of(16, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(17, 35), LocalTime.of(17, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(19, 35), LocalTime.of(19, 55), 1900),
            new ExpressBusCacheInfo(LocalTime.of(21, 5), LocalTime.of(21, 25), 1900),
            new ExpressBusCacheInfo(LocalTime.of(22, 5), LocalTime.of(22, 25), 1900)
        );
    }

    private List<ExpressBusCacheInfo> getTerminalToKoreatech() {
        return List.of(
            new ExpressBusCacheInfo(LocalTime.of(7, 0), LocalTime.of(7, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(9, 0), LocalTime.of(8, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(10, 0), LocalTime.of(10, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(12, 0), LocalTime.of(12, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(13, 0), LocalTime.of(13, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(15, 0), LocalTime.of(15, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(16, 0), LocalTime.of(16, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(18, 0), LocalTime.of(18, 33), 1900),
            new ExpressBusCacheInfo(LocalTime.of(19, 30), LocalTime.of(20, 3), 1900),
            new ExpressBusCacheInfo(LocalTime.of(20, 30), LocalTime.of(21, 3), 1900)
        );
    }
}
