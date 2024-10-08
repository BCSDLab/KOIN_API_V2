package in.koreatech.koin.domain.community.keyword.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordScheduler {

    private final KeywordService keywordService;

    @Scheduled(cron = "0 0 * * * *")
    public void updateRedisWithHotKeywords() {
        try {
            keywordService.fetchTopKeywordsFromLastWeek();
        } catch (Exception e) {
            log.warn("추천 키워드 업데이트 중에 오류가 발생했습니다.");
        }
    }
}

