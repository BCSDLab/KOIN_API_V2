package in.koreatech.koin.domain.community.article.service.summary;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "article.ai-summary")
public class ArticleAiSummaryProperties {

    private static final int MAX_REFINEMENT_RETRY_COUNT_LIMIT = 2;

    private boolean enabled = true;
    private int batchSize = 5;
    private int maxRetryCount = 5;
    private int maxRefinementRetryCount = 1;
    private int lockMinutes = 30;
    private int retryBackoffMinutes = 5;
    private int maxRetryBackoffMinutes = 60;
    private int maxDocumentsPerArticle = 3;
    private int maxDocumentBytes = 10 * 1024 * 1024;
    private int documentParseMinIntervalMillis = 1_000;
    private int requestTimeoutSeconds = 20;
    private String model = "solar-pro3";
    private String promptVersion = "v9";
    private List<String> allowedContentUrlPrefixes = new ArrayList<>();
    private List<String> allowedDocumentUrlPrefixes = new ArrayList<>();

    public int getBoundedMaxRefinementRetryCount() {
        return Math.min(Math.max(maxRefinementRetryCount, 0), MAX_REFINEMENT_RETRY_COUNT_LIMIT);
    }
}
