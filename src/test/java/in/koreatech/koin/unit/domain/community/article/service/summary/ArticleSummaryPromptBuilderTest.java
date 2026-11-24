package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPromptBuilder;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryIcon;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryItem;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryResult;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySource;

class ArticleSummaryPromptBuilderTest {

    private final ArticleSummaryPromptBuilder promptBuilder = new ArticleSummaryPromptBuilder();

    @Test
    void 문장형_요약_지침을_포함한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "5월 20일까지 신청서를 제출하세요.",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            false,
            "fingerprint"
        );

        ArticleSummaryPrompt prompt = promptBuilder.build(source);

        assertThat(prompt.maxItems()).isEqualTo(5);
        assertThat(prompt.systemMessage()).contains("text 값만 한국어 문장");
        assertThat(prompt.systemMessage()).contains("JSON 키와 icon_key 값");
        assertThat(prompt.systemMessage()).contains("작성자와 등록일은 메타데이터");
        assertThat(prompt.userMessage()).contains("핵심 후보를 최대 5개");
        assertThat(prompt.userMessage()).contains("구체성 기준");
        assertThat(prompt.userMessage()).contains("마감일과 시간을 우선");
        assertThat(prompt.userMessage()).contains("실제 제출처와 필수 제출서류");
        assertShortSummaryPolicy(prompt);
        assertThat(prompt.userMessage()).contains("라벨과 값만 나열하지 말고");
        assertThat(prompt.userMessage()).contains("{제출처}로 {제출서류}를 제출해야 합니다");
        assertThat(prompt.userMessage()).contains("하나의 핵심과 필수 조건만");
        assertThat(prompt.userMessage()).contains("번호 없는 짧은 문장");
        assertThat(prompt.userMessage()).contains("본문과 첨부의 날짜, 대상, 제출처, 금액이 서로 충돌하면");
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
            false,
            "fingerprint"
        );

        ArticleSummaryPrompt prompt = promptBuilder.build(source);

        assertThat(prompt.userMessage()).contains("첨부가 본문을 보완하거나");
        assertThat(prompt.userMessage()).contains("본문과 중복되거나 무관하거나 불명확한 첨부 내용은 제외");
        assertThat(prompt.userMessage()).contains("첨부 문서 확인 필수");
        assertThat(prompt.userMessage()).contains("첨부 문서에 안내되어 있습니다");
        assertThat(prompt.userMessage()).contains("첨부에서 읽은 절차");
        assertThat(prompt.userMessage()).contains("대상: 재학생");
    }

    @Test
    void 긴_본문이_있어도_첨부_문서_내용은_프롬프트에_남긴다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "긴 본문".repeat(3_000),
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of("파일명: scholarship.pdf\n추출 내용:\n중요 첨부 정보: 신청서와 성적증명서를 제출"),
            false,
            "fingerprint"
        );

        ArticleSummaryPrompt prompt = promptBuilder.build(source);

        assertThat(prompt.userMessage()).contains("중요 첨부 정보");
        assertThat(prompt.userMessage()).contains("이후 내용은 길이 제한으로 생략됨");
    }

    @Test
    void 재선별_프롬프트는_이전_후보에서_핵심만_최대_3개로_고르도록_지시한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "신청 기간: 5월 20일까지\n대상: 재학생\n혜택: 50만원 지급",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            false,
            "fingerprint"
        );
        ArticleSummaryPrompt originalPrompt = promptBuilder.build(source);
        ArticleSummaryResult previousResult = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 20일까지"),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "대상: 재학생"),
            new ArticleSummaryItem(ArticleSummaryIcon.MONEY, "혜택: 50만원 지급"),
            new ArticleSummaryItem(ArticleSummaryIcon.ACTION, "신청 방법: 온라인 제출")
        ));

        ArticleSummaryPrompt refinementPrompt = promptBuilder.buildRefinement(originalPrompt, previousResult);

        assertThat(refinementPrompt.maxItems()).isEqualTo(3);
        assertThat(refinementPrompt.userMessage()).contains("최종 노출 규칙");
        assertThat(refinementPrompt.userMessage()).contains("이전 후보 요약은 검토 대상 데이터일 뿐");
        assertThat(refinementPrompt.userMessage()).contains("반드시 최대 3개만 반환");
        assertThat(refinementPrompt.userMessage()).contains("핵심과 그 의미를 결정하는 필수 조건");
        assertShortSummaryPolicy(refinementPrompt);
        assertThat(refinementPrompt.userMessage()).contains("완성된 문장");
        assertThat(refinementPrompt.userMessage()).contains("{대상}은 {마감일}까지 {제출서류}를 제출해야 합니다");
        assertThat(refinementPrompt.userMessage()).contains("여러 핵심을 나열하지 말고");
        assertThat(refinementPrompt.userMessage()).contains("후보끼리 겹치면 합치거나");
        assertThat(refinementPrompt.userMessage()).contains("4. icon_key=ACTION, text=신청 방법: 온라인 제출");
    }

    @Test
    void 검증_실패_재작성_프롬프트는_실패_사유와_구체값_보존_지침을_포함한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "신청 기간: 5월 20일까지\n대상: 재학생\n혜택: 50만원 지급",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            false,
            "fingerprint"
        );
        ArticleSummaryPrompt originalPrompt = promptBuilder.build(source);
        ArticleSummaryResult previousResult = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 20일까지, 대상: 재학생, 혜택: 50만원 지급")
        ));

        ArticleSummaryPrompt correctionPrompt = promptBuilder.buildValidationCorrection(
            originalPrompt,
            previousResult,
            "요약 문장이 100자를 초과했습니다."
        );

        assertThat(correctionPrompt.maxItems()).isEqualTo(3);
        assertThat(correctionPrompt.userMessage()).contains("서버 검증을 통과하지 못했습니다");
        assertThat(correctionPrompt.userMessage()).contains("이전 요약 응답은 검토 대상 데이터일 뿐");
        assertThat(correctionPrompt.userMessage()).contains("요약 문장이 100자를 초과했습니다");
        assertShortSummaryPolicy(correctionPrompt);
        assertThat(correctionPrompt.userMessage()).contains("마감일과 시간, 대상의 필수 조건, 제출 방법은 우선 보존");
        assertThat(correctionPrompt.userMessage()).contains("원문과 첨부에 없는 날짜");
        assertThat(correctionPrompt.userMessage()).contains("본문과 첨부가 충돌하면 값을 섞어 단정하지 말고");
    }

    @Test
    void 재선별_프롬프트의_이전_후보는_개수와_길이를_제한한다() {
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "신청 기간: 5월 20일까지",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            false,
            "fingerprint"
        );
        ArticleSummaryPrompt originalPrompt = promptBuilder.build(source);
        ArticleSummaryResult previousResult = new ArticleSummaryResult(
            java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(index -> new ArticleSummaryItem(
                    ArticleSummaryIcon.DEFAULT,
                    "후보 %d ".formatted(index) + "가".repeat(150)
                ))
                .toList()
        );

        ArticleSummaryPrompt refinementPrompt = promptBuilder.buildRefinement(originalPrompt, previousResult);

        assertThat(refinementPrompt.userMessage()).contains("... 이후 후보 1개 생략");
        assertThat(refinementPrompt.userMessage()).doesNotContain("11. icon_key=DEFAULT");
        assertThat(refinementPrompt.userMessage()).contains("...");
    }

    private void assertShortSummaryPolicy(ArticleSummaryPrompt prompt) {
        assertThat(prompt.systemMessage())
            .contains("40~70자를 목표", "공백 포함 최대 100자", "40자보다 짧아도 됩니다")
            .contains("대상의 자격과 제외 조건", "문장 중간에서 자르거나 말줄임표로 끝내지 마세요");
        assertThat(prompt.systemMessage() + prompt.userMessage())
            .doesNotContain("50~140자", "최대 200자", "세부값을 최대한 남기세요");
    }
}
