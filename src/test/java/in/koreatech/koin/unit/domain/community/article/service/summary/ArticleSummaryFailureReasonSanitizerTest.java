package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureReasonSanitizer;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureType;

class ArticleSummaryFailureReasonSanitizerTest {

    private final ArticleSummaryFailureReasonSanitizer sanitizer = new ArticleSummaryFailureReasonSanitizer();

    @Test
    void 민감한_토큰_URL_query_provider_body를_마스킹한다() {
        String reason = "Upstage 요약 API 호출에 실패했습니다. status=429, body={\"url\":\"https://x.com/a.pdf?token=abc\"} "
            + "Authorization: Bearer abc.def up_TESTKEYDOESNOTEXIST1234567890";

        String sanitized = sanitizer.sanitize(reason);

        assertThat(sanitized).contains("status=429");
        assertThat(sanitized).contains("body=<redacted>");
        assertThat(sanitized).doesNotContain("abc.def");
        assertThat(sanitized).doesNotContain("up_TESTKEY");
        assertThat(sanitized).doesNotContain("token=abc");
    }

    @Test
    void 실패_원인을_운영자가_볼_수_있는_유형으로_분류한다() {
        assertThat(sanitizer.classify("Upstage 요약 API 호출에 실패했습니다. status=429"))
            .isEqualTo(ArticleSummaryFailureType.RATE_LIMIT);
        assertThat(sanitizer.classify("Upstage 요약 처리 중 오류가 발생했습니다. cause=PrematureCloseException"))
            .isEqualTo(ArticleSummaryFailureType.NETWORK_TRANSIENT);
        assertThat(sanitizer.classify("Upstage 문서 파싱 API 호출에 실패했습니다. status=500"))
            .isEqualTo(ArticleSummaryFailureType.DOCUMENT_PARSE);
        assertThat(sanitizer.classify("출처에 없는 날짜/숫자 정보가 포함되었습니다. token=5월 21일"))
            .isEqualTo(ArticleSummaryFailureType.VALIDATION);
    }
}
