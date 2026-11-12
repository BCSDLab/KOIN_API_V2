package in.koreatech.koin.unit.domain.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class SupportedLanguageTest {

    @ParameterizedTest
    @CsvSource({"ko-kr,ko-KR", "EN-us,en-US", "zh-hans,zh-Hans", "mn-mn,mn-MN", "uz-latn,uz-Latn", "my-mm,my-MM", "JA-JP,ja-JP"})
    void 대소문자만_다른_지원_코드는_정해진_표기로_변환한다(String input, String canonical) {
        assertThat(SupportedLanguage.fromCode(input).getCode()).isEqualTo(canonical);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"fr-FR", "en", "en_US", " en-US "})
    void 지원하지_않는_요청은_기본값으로_바꾸지_않고_거절한다(String input) {
        assertThatThrownBy(() -> SupportedLanguage.fromCode(input))
            .isInstanceOfSatisfying(CustomException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.UNSUPPORTED_LANGUAGE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"fr-FR", "retired-code"})
    void 미설정이나_지원이_끝난_저장값은_한국어로_해석한다(String storedCode) {
        assertThat(SupportedLanguage.fromStoredCode(storedCode)).isEqualTo(SupportedLanguage.KOREAN);
    }
}
