package in.koreatech.koin.domain.kakao.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.kakao.dto.KakaoSkillResponse;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoBotService {

    private final Clock clock;
    private final DiningRepository diningRepository;

    public String getDiningMenus(String diningTime) {
        StringJoiner result = new StringJoiner("\n");
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
}
