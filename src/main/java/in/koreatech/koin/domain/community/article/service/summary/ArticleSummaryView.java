package in.koreatech.koin.domain.community.article.service.summary;

import java.util.List;

public record ArticleSummaryView(
    Status status,
    List<String> summaryLines
) {

    public ArticleSummaryView {
        summaryLines = summaryLines == null ? List.of() : List.copyOf(summaryLines);
    }

    public static ArticleSummaryView success(List<String> summaryLines) {
        return new ArticleSummaryView(Status.SUCCESS, summaryLines);
    }

    public static ArticleSummaryView pending() {
        return new ArticleSummaryView(Status.PENDING, List.of());
    }

    public static ArticleSummaryView unavailable() {
        return new ArticleSummaryView(Status.UNAVAILABLE, List.of());
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        PENDING,
        UNAVAILABLE
    }
}
