package in.koreatech.koin.domain.kakao.service;

import static in.koreatech.koin.domain.kakao.dto.KakaoSkillResponse.QUICK_ACTION_MESSAGE;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse.InnerBusResponse;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.kakao.dto.KakaoBusRequest;
import in.koreatech.koin.domain.kakao.dto.KakaoSkillResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoBotService {

    private final Clock clock;
    private final DiningRepository diningRepository;
    private final BusService busService;

    public String getDiningMenus(String diningTime) {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        var now = LocalDateTime.now(clock);
        List<Dining> dinings = diningRepository.findAllByDate(now.toLocalDate()).stream()
            .filter(it -> it.getType().getLabel().equals(diningTime))
            .toList();

        if (dinings.isEmpty()) {
            return String.format("금일 %s식사는 운영되지 않습니다.", diningTime);
        }
        for (Dining dining : dinings) {
            result.add(String.format("# %s", dining.getPlace()));
            for (String menu : dining.getMenu()) {
                result.add(menu);
            }
            result.add(String.format("%dkcal", dining.getKcal()));
            result.add(String.format("현금 %d원", dining.getPriceCash()));
            result.add(String.format(" %d원", dining.getPriceCard()));
            result.add("────────────");
        }
        return KakaoSkillResponse.builder()
            .simpleText(result.toString())
            .build();
    }

    public String getBusRemainTime(KakaoBusRequest request) {
        StringJoiner resultNow = new StringJoiner(System.lineSeparator());
        resultNow.add("[바로 도착]");
        StringJoiner resultNext = new StringJoiner(System.lineSeparator());
        resultNext.add("[다음 도착]");

        BusStation depart = BusStation.from(request.depart());
        BusStation arrival = BusStation.from(request.arrival());

        StringJoiner nowBuses = new StringJoiner(System.lineSeparator());
        StringJoiner nextBuses = new StringJoiner(System.lineSeparator());
        for (BusType type : BusType.values()) {
            try {
                BusRemainTimeResponse remainTime = busService.getBusRemainTime(
                    type,
                    depart,
                    arrival
                );
                String nowRemain = getRemainTime(type, remainTime.nowBus());
                if (nowRemain != null) {
                    nowBuses.add(nowRemain);
                }
                String nextRemain = getRemainTime(type, remainTime.nextBus());
                if (nextRemain != null) {
                    nextBuses.add(nextRemain);
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }

        if (nowBuses.length() == 0) {
            resultNow.add("버스 운행정보없음");
        }
        if (nextBuses.length() == 0) {
            resultNow.add("버스 운행정보없음");
        }
        resultNow.add(nowBuses.toString());
        resultNext.add(nextBuses.toString());

        return KakaoSkillResponse.builder()
            .simpleText(resultNow.toString())
            .simpleText(resultNext.toString())
            .build();
    }

    private String getRemainTime(
        BusType type,
        InnerBusResponse response
    ) {
        if (response != null) {
            return String.format("%s, %d시간 %d분 %d초 남음",
                type.getLabel(),
                response.remainTime() / 3600,
                response.remainTime() % 3600 / 60,
                response.remainTime() % 60
            );
        }
        return null;
    }

    public String getBusRoutes() {
        return KakaoSkillResponse.builder()
            .simpleText("선택하세요!")
            .quickReply("한기대→터미널", QUICK_ACTION_MESSAGE, "한기대→터미널")
            .quickReply("한기대→천안역", QUICK_ACTION_MESSAGE, "한기대→천안역")
            .quickReply("터미널→한기대", QUICK_ACTION_MESSAGE, "터미널→한기대")
            .quickReply("터미널→천안역", QUICK_ACTION_MESSAGE, "터미널→천안역")
            .quickReply("천안역→한기대", QUICK_ACTION_MESSAGE, "천안역→한기대")
            .quickReply("천안역→터미널", QUICK_ACTION_MESSAGE, "천안역→터미널")
            .build();
    }
}
