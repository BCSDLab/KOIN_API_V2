package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryIcon;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryView;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleAiSummaryResponse(

    @Schema(description = "AI 요약 상태", example = "SUCCESS", requiredMode = REQUIRED)
    Status status,

    @Schema(description = "AI 요약 항목", requiredMode = REQUIRED)
    List<InnerArticleAiSummaryItemResponse> items
) {

    private static final int MAX_SUMMARY_LINES = 3;
    private static final int MAX_SUMMARY_LINE_LENGTH = 220;

    public static ArticleAiSummaryResponse from(ArticleSummaryView summaryView) {
        List<InnerArticleAiSummaryItemResponse> items = summaryView.summaryLines().stream()
            .filter(line -> line != null && !line.isBlank())
            .filter(line -> line.length() <= MAX_SUMMARY_LINE_LENGTH)
            .map(InnerArticleAiSummaryItemResponse::from)
            .flatMap(Optional::stream)
            .limit(MAX_SUMMARY_LINES)
            .toList();
        Status status = summaryView.isSuccess() && items.isEmpty()
            ? Status.UNAVAILABLE
            : Status.valueOf(summaryView.status().name());
        return new ArticleAiSummaryResponse(status, items);
    }

    public enum Status {
        SUCCESS,
        PENDING,
        UNAVAILABLE
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerArticleAiSummaryItemResponse(

        @Schema(description = "요약 항목 아이콘", example = "📅", requiredMode = REQUIRED)
        String icon,

        @Schema(description = "요약 내용", example = "신청은 5월 20일까지 접수됩니다.", requiredMode = REQUIRED)
        String text
    ) {

        private static Optional<InnerArticleAiSummaryItemResponse> from(String summaryLine) {
            if (summaryLine == null || summaryLine.isBlank()) {
                return Optional.empty();
            }
            return Arrays.stream(ArticleSummaryIcon.values())
                .filter(icon -> summaryLine.startsWith(icon.getEmoji() + " "))
                .findFirst()
                .map(icon -> new InnerArticleAiSummaryItemResponse(
                    icon.getEmoji(),
                    summaryLine.substring((icon.getEmoji() + " ").length()).trim()
                ))
                .filter(item -> !item.text().isBlank());
        }
    }
}
