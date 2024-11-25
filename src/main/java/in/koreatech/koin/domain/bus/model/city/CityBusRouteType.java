package in.koreatech.koin.domain.bus.model.city;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.CityBusDirection;

public sealed interface CityBusRouteType {

    CityBusDirection getDepartStation();
    CityBusDirection getArrivalStation();
    Long getBusNumber();

    Map<Long, CityBusDirection> CITY_BUS_INFO = Map.of(
        400L, CityBusDirection.병천3리,
        402L, CityBusDirection.황사동,
        405L, CityBusDirection.유관순열사사적지
    );

    static List<CityBusRouteType> findRoutes(BusStation depart, BusStation arrive) {
        return switch (depart) {
            case KOREATECH -> {
                if (arrive == BusStation.TERMINAL) {
                    yield CITY_BUS_INFO.keySet().stream()
                        .map(busNumber -> new KoreaTechToTerminal(CITY_BUS_INFO.get(busNumber), busNumber))
                        .collect(Collectors.toList());
                } else
                    yield Collections.emptyList();
            }
            case TERMINAL -> {
                if (arrive == BusStation.KOREATECH) {
                    yield CITY_BUS_INFO.entrySet().stream()
                        .map(entry -> new TerminalToKoreaTech(entry.getValue(), entry.getKey()))
                        .collect(Collectors.toList());
                } else
                    yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }

    record KoreaTechToTerminal(CityBusDirection departStation, Long busNumber) implements CityBusRouteType {
        @Override
        public CityBusDirection getDepartStation() {
            return departStation;
        }

        @Override
        public CityBusDirection getArrivalStation() {
            return CityBusDirection.종합터미널;
        }

        @Override
        public Long getBusNumber() {
            return busNumber;
        }
    }

    record TerminalToKoreaTech(CityBusDirection arriveStation, Long busNumber) implements CityBusRouteType {
        @Override
        public CityBusDirection getDepartStation() {
            return CityBusDirection.종합터미널;
        }

        @Override
        public CityBusDirection getArrivalStation() {
            return arriveStation;
        }

        @Override
        public Long getBusNumber() {
            return busNumber;
        }
    }
}
