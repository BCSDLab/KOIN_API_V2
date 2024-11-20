package in.koreatech.koin.domain.bus.batch.response;

import lombok.Builder;

@Builder
public record PublicOpenApiExpressBusArrival(
    String arrPlaceNm,      // 도착지
    String arrPlandTime,    // 도착 시간
    String depPlaceNm,      // 출발지
    String depPlandTime,    // 출발 시간
    int charge,             // 운임 요금
    String gradeNm,         // 버스 등급
    String routeId          // 노선 id
) {

}
