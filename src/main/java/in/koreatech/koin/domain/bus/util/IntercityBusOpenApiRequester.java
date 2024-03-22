package in.koreatech.koin.domain.bus.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.model.Bus;
import lombok.RequiredArgsConstructor;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098530
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntercityBusOpenApiRequester extends BusOpenApiRequester<Bus> {

    @Override
    public List<Bus> getBusRemainTime(String nodeId) {
        return new ArrayList<>();
    }
}
