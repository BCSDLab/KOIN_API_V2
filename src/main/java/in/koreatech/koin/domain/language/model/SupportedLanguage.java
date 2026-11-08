package in.koreatech.koin.domain.language.model;

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
}
