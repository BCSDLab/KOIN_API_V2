package in.koreatech.koin.domain.community.article.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.service.ArticleSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private final ArticleSyncService articleSyncService;

    @Scheduled(cron = "0 0 6 * * *")
    public void updateHotArticles() {
        try {
            articleSyncService.updateHotArticles();
        } catch (Exception e) {
            log.error("인기 게시글 업데이트 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void getBusNoticeArticle() {
        try {
            articleSyncService.updateBusNoticeArticle();
        } catch (Exception e) {
            log.error("버스 공지 게시글 조회 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void synchronizeKeywords() {
        try {
            articleSyncService.resetWeightsAndCounts();
            articleSyncService.synchronizeKeywords();
        } catch (Exception e) {
            log.error("Redis에서 MySQL로 키워드 동기화 중 오류가 발생했습니다.", e);
        }
    }
}
