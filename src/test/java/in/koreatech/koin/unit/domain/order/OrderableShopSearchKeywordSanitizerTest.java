package in.koreatech.koin.unit.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.order.shop.service.OrderableShopSearchKeywordSanitizer;

public class OrderableShopSearchKeywordSanitizerTest {

    private OrderableShopSearchKeywordSanitizer keywordSanitizer;

    @BeforeEach
    void setUp() {
        keywordSanitizer = new OrderableShopSearchKeywordSanitizer();
    }

    @Test
    void 빈_문자열_입력시_빈_리스트를_반환한다() {
        // given
        String input = "";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 공백만_있는_문자열_입력시_빈_리스트를_반환한다() {
        // given
        String input = "   ";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 단일_키워드를_정상적으로_정제한다() {
        // given
        String input = "짜장면";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result)
            .hasSize(1)
            .containsExactly("짜장면");
    }

    @Test
    void 여러_키워드를_공백을_기준으로_분리하여_정제한다() {
        // given
        String input = "페리카나 치킨";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result)
            .hasSize(2)
            .containsExactly("페리카나", "치킨");
    }

    @Test
    void 불완전한_한글이_포함된_키워드를_정제한다() {
        // given
        String input = "페리ㅣ카나ㅊ 치킨ㅋ";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result)
            .hasSize(2)
            .containsExactly("페리카나", "치킨");
    }

    @Test
    void 특수문자가_포함된_키워드를_정제한다() {
        // given
        String input = "치킨, 피자";

        // when
        List<String> result = keywordSanitizer.sanitizeToKeywords(input);

        // then
        assertThat(result)
            .hasSize(2)
            .containsExactly("치킨", "피자");
    }
}
