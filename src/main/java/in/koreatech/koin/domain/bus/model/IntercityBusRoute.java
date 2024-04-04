package in.koreatech.koin.domain.bus.model;

import in.koreatech.koin.domain.bus.model.enums.IntercityBusStationNode;

public record IntercityBusRoute(
    String depTerminalId,      // 출발지
    String arrTerminalId       // 도착지
) {
    public static IntercityBusRoute from(IntercityBusArrival intercityBusArrival) {
        return new IntercityBusRoute(
            IntercityBusStationNode.getId(intercityBusArrival.depPlaceNm()),
            IntercityBusStationNode.getId(intercityBusArrival.arrPlaceNm())
        );
    }
}
