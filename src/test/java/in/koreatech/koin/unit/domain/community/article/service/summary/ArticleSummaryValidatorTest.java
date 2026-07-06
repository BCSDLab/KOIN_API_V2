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
            .isInstanceOf(ArticleSummaryValidationException.class)
            .hasMessageContaining("token=5월 21일");
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
    void 축약_연도_날짜_범위의_끝일자는_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "행사는 2025년 2월 14일까지 진행되었습니다.")
        ));

        List<String> lines = validator.validate(result, "25.2.10.(월) ~ 14.(금) 5일 간 진행된 행사입니다.");

        assertThat(lines).containsExactly("📅 행사는 2025년 2월 14일까지 진행되었습니다.");
    }

    @Test
    void 월일_범위의_끝일자는_한글_월일_표기와_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "운영 기간은 6월 20일까지입니다.")
        ));

        List<String> lines = validator.validate(result, "3.4~6.20 운영 예정");

        assertThat(lines).containsExactly("📅 운영 기간은 6월 20일까지입니다.");
    }

    @Test
    void 공백과_요일이_포함된_월일과_시간_표기를_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청은 5월 6일 23:59까지 접수됩니다.")
        ));

        List<String> lines = validator.validate(result, "5. 6.(월) 23:59까지 접수");

        assertThat(lines).containsExactly("📅 신청은 5월 6일 23:59까지 접수됩니다.");
    }

    @Test
    void 시간_범위는_콜론과_시_표현을_같은_출처_정보로_인정한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "상담은 9시부터 18시까지 운영됩니다.")
        ));

        List<String> lines = validator.validate(result, "상담 운영 시간: 09:00~18:00");

        assertThat(lines).containsExactly("📅 상담은 9시부터 18시까지 운영됩니다.");
    }

    @Test
    void 날짜_문맥이_없는_버전_숫자는_날짜로_과검출하지_않는다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "라이브러리는 v1.1 버전을 사용합니다.")
        ));

        List<String> lines = validator.validate(result, "라이브러리는 v1.0 버전을 사용합니다.");

        assertThat(lines).containsExactly("✅ 라이브러리는 v1.1 버전을 사용합니다.");
    }

    @Test
    void 일부_항목만_출처_검증에_실패하면_정상_항목은_반환하고_실패_원인을_남긴다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청은 5월 21일까지 접수됩니다."),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "대상은 재학생입니다.")
        ));

        ArticleSummaryValidator.ValidationResult validationResult = validator.validateFilteringInvalidItems(
            result,
            "신청은 5월 20일까지 접수됩니다. 대상은 재학생입니다."
        );

        assertThat(validationResult.validLines()).containsExactly("🎯 대상은 재학생입니다.");
        assertThat(validationResult.failureReasons()).hasSize(1);
        assertThat(validationResult.firstFailureReason())
            .contains("token=5월 21일")
            .contains("item=신청은 5월 21일까지 접수됩니다.");
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

    @Test
    void 첨부_위치만_안내하는_요약은_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DOCUMENT, "재 로그인 절차는 첨부 문서에 안내되어 있습니다.")
        ));

        assertThatThrownBy(() -> validator.validate(result, "첨부 문서/이미지 추출 내용 로그아웃 후 재 로그인"))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }

    @Test
    void 첨부_위치와_안내_표현_사이에_단어가_있어도_실패한다() {
        ArticleSummaryResult result = new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DOCUMENT, "첨부 문서에 재로그인 절차가 안내되어 있습니다.")
        ));

        assertThatThrownBy(() -> validator.validate(result, "첨부 문서/이미지 추출 내용 로그아웃 후 재 로그인"))
            .isInstanceOf(ArticleSummaryValidationException.class);
    }
}
