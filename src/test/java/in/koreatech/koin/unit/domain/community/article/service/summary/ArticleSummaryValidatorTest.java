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
}
