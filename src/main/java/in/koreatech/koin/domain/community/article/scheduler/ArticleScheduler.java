package in.koreatech.koin.domain.community.article.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private final ArticleService articleService;

    @Scheduled(cron = "0 0 6 * * *")
    public void updateHotArticles() {
        try {
            articleService.updateHotArticles();
        } catch (Exception e) {
            log.error("인기 게시글 업데이트 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 0/6 * * *")
    public void resetOldKeywordsAndIpMaps() {
        try {
            articleService.resetWeightsAndCounts();
        } catch (Exception e) {
            log.error("많이 검색한 키워드 초기화 중에 오류가 발생했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void getBusNoticeArticle() {
        try {
            articleService.updateBusNoticeArticle();
        } catch (Exception e) {
            log.error("버스 공지 게시글 조회 중에 오류가 발생했습니다.", e);
        }
    }
}
