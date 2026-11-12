package in.koreatech.koin.domain.language.model;

import java.util.Arrays;
import java.util.Optional;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedLanguage {
    KOREAN("ko-KR", "한국어"),
    ENGLISH("en-US", "English"),
    SIMPLIFIED_CHINESE("zh-Hans", "简体中文"),
    MONGOLIAN("mn-MN", "Монгол"),
    UZBEK("uz-Latn", "O‘zbekcha"),
    BURMESE("my-MM", "မြန်မာဘာသာ"),
    JAPANESE("ja-JP", "日本語");

    public static final SupportedLanguage DEFAULT = KOREAN;

    private final String code;
    private final String name;

    public static SupportedLanguage fromCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.UNSUPPORTED_LANGUAGE));
    }

    public static SupportedLanguage fromStoredCode(String code) {
        return findByCode(code).orElse(DEFAULT);
    }

    private static Optional<SupportedLanguage> findByCode(String code) {
        return Arrays.stream(values())
            .filter(language -> language.code.equalsIgnoreCase(code))
            .findFirst();
    }
}
