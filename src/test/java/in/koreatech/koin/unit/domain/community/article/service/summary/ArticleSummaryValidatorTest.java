package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryIcon;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryItem;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryResult;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryValidationException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryValidator;

class ArticleSummaryValidatorTest {

    private final ArticleSummaryValidator validator = new ArticleSummaryValidator();

    @Test
    void 요약_문장을_검증하고_서버가_이모지를_붙인다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청은 5월 20일까지 접수됩니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "재학생을 대상으로 모집합니다.")
        ));

        List<String> lines = validator.validate(result, "신청은 5월 20일까지 접수됩니다. 재학생 대상 모집");

        assertThat(lines).containsExactly(
            "📅 신청은 5월 20일까지 접수됩니다.",
            "🎯 재학생을 대상으로 모집합니다."
        );
    }

    @Test
    void 모델이_넣은_이모지는_제거하고_서버_이모지만_사용한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.NOTICE, "🔥 변경사항은 5월 20일에 공지됩니다.")
        ));

        List<String> lines = validator.validate(result, "변경사항은 5월 20일에 공지됩니다.");

        assertThat(lines).containsExactly("📌 변경사항은 5월 20일에 공지됩니다.");
    }

    @Test
    void 요약이_4개면_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "첫 번째 문장입니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "두 번째 문장입니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "세 번째 문장입니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "네 번째 문장입니다.")
        ));

        assertThatThrownBy(() -> validator.validate(result, "첫 번째 두 번째 세 번째 네 번째"))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 출처에_없는_날짜가_있으면_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청은 5월 21일까지 접수됩니다.")
        ));

        assertThatThrownBy(() -> validator.validate(result, "신청은 5월 20일까지 접수됩니다."))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 요약_문장은_200자까지_허용한다() {
        String text = "가".repeat(200);
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, text)
        ));

        List<String> lines = validator.validate(result, text);

        assertThat(lines).containsExactly("✅ " + text);
    }

    @Test
    void 요약_문장이_200자를_초과하면_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "가".repeat(201))
        ));

        assertThatThrownBy(() -> validator.validate(result, "가".repeat(201)))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 날짜와_시간의_앞자리_0_차이는_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "모집일정: 2018.12.28~2019.1.20, 9시부터 접수")
        ));

        List<String> lines = validator.validate(result, "모집일정: 2018. 12. 28. ~ 2019. 01. 20., 09시부터 접수");

        assertThat(lines).containsExactly("📅 모집일정: 2018.12.28~2019.1.20, 9시부터 접수");
    }

    @Test
    void 정시_표기는_콜론과_시_표현을_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "마감시간: 24시까지")
        ));

        List<String> lines = validator.validate(result, "마감시간: 24:00까지");

        assertThat(lines).containsExactly("📅 마감시간: 24시까지");
    }

    @Test
    void 축약_연도_날짜는_4자리_연도_표기와_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "일정: 2024.09.26 GEC-DAY 진행")
        ));

        List<String> lines = validator.validate(result, "24.09.26(목) GEC-DAY 안내");

        assertThat(lines).containsExactly("📅 일정: 2024.09.26 GEC-DAY 진행");
    }

    @Test
    void 숫자_월일_표기는_한글_월일_표기와_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "일정: 10월 2일 진행")
        ));

        List<String> lines = validator.validate(result, "10/2(수) 프로그램 운영");

        assertThat(lines).containsExactly("📅 일정: 10월 2일 진행");
    }

    @Test
    void 빈_문장이_있으면_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, " ")
        ));

        assertThatThrownBy(() -> validator.validate(result, "본문"))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 중복_문장이_있으면_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "재학생을 대상으로 모집합니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "재학생을 대상으로 모집합니다.")
        ));

        assertThatThrownBy(() -> validator.validate(result, "재학생을 대상으로 모집합니다."))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 첨부_확인만_요구하는_요약은_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DOCUMENT, "첨부 문서 확인 필수")
        ));

        assertThatThrownBy(() -> validator.validate(result, "첨부 문서/이미지 추출 내용 대상 재학생"))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }
}
