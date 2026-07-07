package in.koreatech.koin.admin.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.article.dto.AdminArticleAiSummariesResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryOverviewResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryResponse;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) ArticleAiSummary: 게시글 AI 요약", description = "관리자 권한으로 게시글 AI 요약 작업 상태를 조회한다")
public interface AdminArticleAiSummaryApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "게시글 AI 요약 큐 상태 요약 조회")
    @GetMapping("/admin/articles/ai-summaries/overview")
    ResponseEntity<AdminArticleAiSummaryOverviewResponse> getOverview(
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "게시글 AI 요약 작업 목록 조회")
    @GetMapping("/admin/articles/ai-summaries")
    ResponseEntity<AdminArticleAiSummariesResponse> getSummaries(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "status", required = false) ArticleAiSummaryStatus status,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "게시글 AI 요약 작업 단건 조회")
    @GetMapping("/admin/articles/ai-summaries/{summaryId}")
    ResponseEntity<AdminArticleAiSummaryResponse> getSummary(
        @Parameter(in = PATH) @PathVariable Integer summaryId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
