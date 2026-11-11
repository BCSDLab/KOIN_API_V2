package in.koreatech.koin.domain.community.article.service.summary;

import java.time.Duration;
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
    private static final int MAX_DOCUMENT_BYTES_LIMIT = 50 * 1024 * 1024;

    private boolean enabled = false;
    private int batchSize = 5;
    private int maxRetryCount = 10;
    private int maxRefinementRetryCount = 1;
    private int lockMinutes = 30;
    private int retryBackoffMinutes = 5;
    private int maxRetryBackoffMinutes = 60;
    private int maxDocumentsPerArticle = 3;
    private int maxDocumentBytes = 50 * 1024 * 1024;
    private String documentParseOcrMode = "auto";
    private int documentParseMinIntervalMillis = 1_000;
    private int failedRetryWindowStartHour = 0;
    private int failedRetryWindowEndHour = 4;
    private int requestTimeoutSeconds = 120;
    private int chatRequestTimeoutSeconds = 600;
    private int documentParseRequestTimeoutSeconds = 180;
    private int documentDownloadTimeoutSeconds = 60;
    private String model = "solar-pro4";
    private String promptVersion = "v12";
    private List<String> allowedContentUrlPrefixes = new ArrayList<>(
        List.of("https://*.koreatech.in/", "https://*.koreatech.ac.kr/")
    );
    private List<String> allowedDocumentUrlPrefixes = new ArrayList<>(
        List.of("https://*.koreatech.in/", "https://*.koreatech.ac.kr/")
    );

    public int getBoundedMaxRefinementRetryCount() {
        return Math.min(Math.max(maxRefinementRetryCount, 0), MAX_REFINEMENT_RETRY_COUNT_LIMIT);
    }

    public Duration getProcessingLockDuration() {
        int refinements = getBoundedMaxRefinementRetryCount();
        // 최초 생성 + 재선별 + 검증 보정 + 이전 기준 생성 1회.
        int maxSummaryCalls = 1 + refinements + (refinements > 0 ? 1 : 0) + 1;
        Duration generationBudget = Duration.ofSeconds(chatRequestTimeoutSeconds).multipliedBy(maxSummaryCalls);
        Duration documentBudget = Duration.ofSeconds((long) documentDownloadTimeoutSeconds + documentParseRequestTimeoutSeconds)
            .plusMillis(documentParseMinIntervalMillis)
            .multipliedBy(maxDocumentsPerArticle);
        Duration minimumLock = generationBudget.plus(documentBudget).plusMinutes(1);
        Duration configuredLock = Duration.ofMinutes(lockMinutes);
        return configuredLock.compareTo(minimumLock) >= 0 ? configuredLock : minimumLock;
    }

    public int getBoundedMaxDocumentBytes() {
        return Math.min(Math.max(maxDocumentBytes, 1), MAX_DOCUMENT_BYTES_LIMIT);
    }

    public int getBoundedFailedRetryWindowStartHour() {
        return Math.min(Math.max(failedRetryWindowStartHour, 0), 23);
    }

    public int getBoundedFailedRetryWindowEndHour() {
        return Math.min(Math.max(failedRetryWindowEndHour, 0), 24);
    }
}
