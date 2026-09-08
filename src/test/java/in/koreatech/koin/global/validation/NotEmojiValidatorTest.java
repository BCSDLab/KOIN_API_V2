package in.koreatech.koin.global.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotEmojiValidatorTest {

    private final NotEmojiValidator validator = new NotEmojiValidator();

    @Test
    void ASCII_하이픈을_허용한다() {
        assertThat(validator.isValid("user-1", null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc_d", "abc.d", "abc123"})
    void 회원가입_아이디에_허용된_문자를_허용한다(String field) {
        assertThat(validator.isValid(field, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"😀", "abc😀d", "🫠", "❤️", "1️⃣"})
    void 실제_이모지를_거절한다(String field) {
        assertThat(validator.isValid(field, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0xD83D, 0xDC00})
    void 고립된_surrogate를_거절한다(int surrogate) {
        String field = new String(new char[]{(char)surrogate});

        assertThat(validator.isValid(field, null)).isFalse();
    }

    @Test
    void variation_selector가_없는_기존_BMP_문자는_허용한다() {
        assertThat(validator.isValid("☕", null)).isTrue();
    }

    @Test
    void 빈_문자열은_허용한다() {
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    void null은_허용한다() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
