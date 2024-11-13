package in.koreatech.koin.domain.bus.city.dto;

import lombok.Builder;

@Builder
public record CityBusRoute(
    String endnodenm, // 종점, 병천3리
    String routeid, // 노선 ID, CAB285000142
    Long routeno, // 노선 번호, 400
    String routetp, // 노선 유형, 일반버스
    String startnodenm // 기점, 종합터미널
) {

}
