package in.koreatech.koin.domain.community.article.service;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.LostItemReportRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemReportRequest.InnerLostItemReport;
import in.koreatech.koin.domain.community.article.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemReport;
import in.koreatech.koin._common.event.LostItemReportEvent;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemReportRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemReportService {

    public static final int LOST_ITEM_BOARD_ID = 14;

    private final StudentRepository studentRepository;
    private final ArticleRepository articleRepository;
    private final LostItemReportRepository lostItemReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reportLostItemArticle(Integer articleId, Integer studentId,
        LostItemReportRequest lostItemReportRequest) {
        Student student = studentRepository.getById(studentId);
        Article article = articleRepository.getById(articleId);
        validateLostItemArticle(article);
        lostItemReportRequest.reports().forEach(reportRequest -> saveReport(reportRequest, student, article));
        eventPublisher.publishEvent(new LostItemReportEvent(article.getId()));
    }

    /**
     * 분실물 게시글인지 검증하는 메서드
     * @param article 검증할 게시글 객체
     * @throws ArticleBoardMisMatchException 분실물 게시글이 아닌 경우 발생하는 예외
     */
    private static void validateLostItemArticle(Article article) {
        if (article.getBoard().getId() != LOST_ITEM_BOARD_ID) {
            throw ArticleBoardMisMatchException.withDetail(
                "boardId: " + article.getBoard().getId() + ", articleId: " + article.getId());
        }
    }

    private void saveReport(InnerLostItemReport reportRequest, Student student, Article article) {
        LostItemReport report = LostItemReport.builder()
            .student(student)
            .lostItemArticle(article.getLostItemArticle())
            .title(reportRequest.title())
            .reportStatus(UNHANDLED)
            .content(reportRequest.content())
            .build();
        lostItemReportRepository.save(report);
    }
}
