package in.koreatech.koin.domain.bus.city.dto;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.bus.city.model.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.global.model.BusRemainTime;
import lombok.Builder;

@Builder
public record CityBusArrival(
    Long arrprevstationcnt, // 남은 정거장 개수, 5
    Long arrtime, // 도착까지 남은 시간 [초], 222
    String nodeid, // 정류소 id, "CAB285000405"
    String nodenm, // 정류소명, "코리아텍"
    String routeid, // 노선 id, "CAB285000147"
    Long routeno, // 버스 번호, 402
    String routetp, // 노선 유형, "일반 버스"
    String vehicletp // 차량 유형 (저상버스), "일반차량"
) {

    /**
     * <pre>
     * 남은 시간 = (캐시 저장 시각 + 저장된 남은시간) - 현재시각
     * {@link BusRemainTime#getRemainSeconds}에서는 남은 시간을 (저장된 남은시간 - 현재시각)으로 계산중
     * 학교 버스는 도착 시간을 저장하고, 시내버스는 남은 시간만을 저장하므로
     * Redis에 저장할때 (캐시 저장 시각 + 남은시간)으로 저장하여 통일시켜줌</pre>
     * */
    public CityBusCacheInfo toCityBusCacheInfo(LocalDateTime updatedAt) {
        return new CityBusCacheInfo(
            routeno,
            updatedAt.plusSeconds(arrtime).toLocalTime()
        );
    }
}
