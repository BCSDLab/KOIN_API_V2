package in.koreatech.koin.domain.bus.service.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.service.city.client.CityBusClient;
import in.koreatech.koin.domain.bus.service.city.client.CityBusRouteClient;
import in.koreatech.koin.domain.bus.service.express.client.ExpressBusClient;
import in.koreatech.koin._common.callcontoller.CallController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusCacheService {

    private final CityBusClient cityBusClient;
    private final CityBusRouteClient cityBusRouteClient;
    private final List<ExpressBusClient> expressBusTypes;
    private final List<ExpressBusClient> apiCallListByRatio = new ArrayList<>();
    private final CallController<ExpressBusClient> callController;

    public void cacheCityBusByOpenApi() {
        cityBusClient.storeRemainTime();
        cityBusRouteClient.storeCityBusRoute();
    }

    public void storeRemainTimeByRatio() {  // callController를 통해서 호출 비율을 보장해주고, 서킷브레이커를 통한 다중화
        ExpressBusClient selectedBus = callController.getInstanceByRatio(expressBusTypes, apiCallListByRatio);
        List<ExpressBusClient> fallBackableTypes = new ArrayList<>(expressBusTypes);
        while (true) {
            try {
                selectedBus.storeRemainTime();
                break;
            } catch (IndexOutOfBoundsException e) {
                throw new BusOpenApiException("호출할 수 있는 버스 API가 없습니다.");
            } catch (Exception e) {
                log.warn(String.format("%s 호출 중 문제가 발생했습니다.", selectedBus));
                selectedBus = callController.fallBack(selectedBus, fallBackableTypes);
            }
        }
    }
}
