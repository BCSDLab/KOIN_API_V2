package in.koreatech.koin.domain.community.article.service.summary;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ArticleSummaryFailureReasonSanitizer {

    private static final int MAX_LENGTH = 500;
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)bearer\\s+[a-z0-9._~+/=-]+");
    private static final Pattern UPSTAGE_KEY_PATTERN = Pattern.compile("up_[A-Za-z0-9]+");
    private static final Pattern URL_QUERY_PATTERN = Pattern.compile("(https?://[^\\s,]+)\\?[^\\s,]*");
    private static final Pattern BODY_PATTERN = Pattern.compile("(?i)(,?\\s*body=).*$");

    public String sanitize(String reason) {
        if (!StringUtils.hasText(reason)) {
            return null;
        }
        String sanitized = reason.replaceAll("\\s+", " ").trim();
        sanitized = BEARER_TOKEN_PATTERN.matcher(sanitized).replaceAll("Bearer <redacted>");
        sanitized = UPSTAGE_KEY_PATTERN.matcher(sanitized).replaceAll("up_<redacted>");
        sanitized = URL_QUERY_PATTERN.matcher(sanitized).replaceAll("$1?<redacted>");
        sanitized = BODY_PATTERN.matcher(sanitized).replaceAll("$1<redacted>");
        return truncate(sanitized);
    }

    public ArticleSummaryFailureType classify(String reason) {
        String sanitized = sanitize(reason);
        if (!StringUtils.hasText(sanitized)) {
            return ArticleSummaryFailureType.UNKNOWN;
        }
        String value = sanitized.toLowerCase();
        if (value.contains("status=429") || value.contains("rate limit") || value.contains("too many requests")) {
            return ArticleSummaryFailureType.RATE_LIMIT;
        }
        if (value.contains("문서 다운로드")) {
            return ArticleSummaryFailureType.DOCUMENT_DOWNLOAD;
        }
        if (value.contains("문서 파싱") || value.contains("document parse")) {
            return ArticleSummaryFailureType.DOCUMENT_PARSE;
        }
        if (value.contains("prematurecloseexception") || value.contains("timeout") || value.contains("timed out")) {
            return ArticleSummaryFailureType.NETWORK_TRANSIENT;
        }
        if (value.contains("status=5")) {
            return ArticleSummaryFailureType.UPSTAGE_SERVER_ERROR;
        }
        if (value.contains("status=4")) {
            return ArticleSummaryFailureType.UPSTAGE_CLIENT_ERROR;
        }
        if (value.contains("json 파싱") || value.contains("응답이 비어") || value.contains("본문이 비어")) {
            return ArticleSummaryFailureType.MODEL_RESPONSE_INVALID;
        }
        if (value.contains("요약에 사용할 본문") || value.contains("내용이 없습니다")) {
            return ArticleSummaryFailureType.EMPTY_SOURCE;
        }
        if (value.contains("요약할 핵심 정보") || value.contains("결과가 비어")) {
            return ArticleSummaryFailureType.EMPTY_RESULT;
        }
        if (value.contains("upstage api key")) {
            return ArticleSummaryFailureType.CONFIG;
        }
        if (value.contains("검증") || value.contains("출처에 없는") || value.contains("초과") || value.contains("중복")) {
            return ArticleSummaryFailureType.VALIDATION;
        }
        return ArticleSummaryFailureType.UNKNOWN;
    }

    private String truncate(String value) {
        if (value.length() <= MAX_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LENGTH);
    }
}
