package in.koreatech.koin.domain.community.article.service.summary;

public enum ArticleSummaryFailureType {
    RATE_LIMIT,
    NETWORK_TRANSIENT,
    UPSTAGE_SERVER_ERROR,
    UPSTAGE_CLIENT_ERROR,
    DOCUMENT_DOWNLOAD,
    DOCUMENT_PARSE,
    MODEL_RESPONSE_INVALID,
    VALIDATION,
    EMPTY_SOURCE,
    EMPTY_RESULT,
    CONFIG,
    UNKNOWN
}
