package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPromptBuilder;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySource;

class ArticleSummaryPromptBuilderTest {

    private final ArticleSummaryPromptBuilder promptBuilder = new ArticleSummaryPromptBuilder();

    @Test
    void 개괄식_요약_지침을_포함한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "5월 20일까지 신청서를 제출하세요.",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            "fingerprint"
        );

        ArticleSummaryPrompt prompt = promptBuilder.build(source);

        assertThat(prompt.userMessage()).contains("개괄식 표현");
        assertThat(prompt.userMessage()).contains("신청 기간: 5월 20일까지");
        assertThat(prompt.userMessage()).contains("마침표는 생략");
    }

    @Test
    void 첨부_문서_내용을_구체적으로_요약하라는_지침을_포함한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "첨부파일을 확인하세요.",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of("파일명: scholarship.pdf\n추출 내용:\n대상: 재학생\n제출 서류: 신청서"),
            "fingerprint"
        );

        ArticleSummaryPrompt prompt = promptBuilder.build(source);

        assertThat(prompt.userMessage()).contains("첨부 내용을 반드시 본문과 함께 읽고");
        assertThat(prompt.userMessage()).contains("첨부 문서 확인 필수");
        assertThat(prompt.userMessage()).contains("대상: 재학생");
    }
}
