package in.koreatech.koin.domain.bus.batch.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.batch.client.ExpressBusClient;
import in.koreatech.koin.domain.bus.batch.exception.BusOpenApiException;
import in.koreatech.koin.global.domain.callcontoller.CallController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExpressBusCacheService {

    private final List<ExpressBusClient> expressBusTypes;
    private final List<ExpressBusClient> apiCallListByRatio = new ArrayList<>();
    private final CallController<ExpressBusClient> callController;

    public void cacheRemainTimeByRatio() {
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
