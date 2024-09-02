package in.koreatech.koin.domain.community.article.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private final CommunityService communityService;

    @Scheduled(cron = "0 0 0/6 * * *")
    public void resetOldKeywordsAndIpMaps() {
        try {
            communityService.resetWeightsAndCounts();
        } catch (Exception e) {
            log.error("많이 검색한 키워드 초기화 중에 오류가 발생했습니다.", e);
        }
    }
}