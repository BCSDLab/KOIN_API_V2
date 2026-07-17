package in.koreatech.koin.admin.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.article.dto.AdminArticleAiSummariesResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryOverviewResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryResponse;
import in.koreatech.koin.admin.article.service.AdminArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminArticleAiSummaryController implements AdminArticleAiSummaryApi {

    private final AdminArticleAiSummaryService adminArticleAiSummaryService;

    @Override
    @GetMapping("/admin/articles/ai-summaries/overview")
    public ResponseEntity<AdminArticleAiSummaryOverviewResponse> getOverview(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok(adminArticleAiSummaryService.getOverview());
    }

    @Override
    @GetMapping("/admin/articles/ai-summaries")
    public ResponseEntity<AdminArticleAiSummariesResponse> getSummaries(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "status", required = false) ArticleAiSummaryStatus status,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok(adminArticleAiSummaryService.getSummaries(page, limit, status));
    }

    @Override
    @GetMapping("/admin/articles/ai-summaries/{summaryId}")
    public ResponseEntity<AdminArticleAiSummaryResponse> getSummary(
        @PathVariable Integer summaryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok(adminArticleAiSummaryService.getSummary(summaryId));
    }
}
