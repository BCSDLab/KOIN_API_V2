package in.koreatech.koin.infrastructure.upstage.client;

import java.lang.Character.UnicodeScript;
import java.util.Map;
import java.util.regex.Pattern;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.service.TranslationFailureException;

/** 다른 문자 체계의 새 문구를 차단한다. 같은 문자를 쓰는 언어의 의미·품질 판정은 하지 않는다. */
final class TranslationLanguageGuard {
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']+");

    private TranslationLanguageGuard() {
    }

    static String targetDescription(SupportedLanguage language) {
        return switch (language) {
            case KOREAN -> "Korean in Hangul";
            case ENGLISH -> "English";
            case SIMPLIFIED_CHINESE -> "Simplified Chinese";
            case MONGOLIAN -> "Mongolian in Cyrillic script";
            case UZBEK -> "Uzbek in Latin script";
            case BURMESE -> "Burmese (Myanmar language) in Myanmar script";
            case JAPANESE -> "Japanese";
        };
    }

    static void validate(TranslationTextProtection source, Map<String, String> translated,
        SupportedLanguage language) {
        for (var field : translated.entrySet()) {
            String original = source.fields().get(field.getKey());
            String output = field.getValue();
            // 복원할 숫자·통화와 원문에 있던 URL만 검사에서 제외한다.
            for (String token : source.tokens().get(field.getKey()).keySet()) {
                original = original.replace(token, " ");
                output = output.replace(token, " ");
            }
            var urls = URL.matcher(source.originals().get(field.getKey()));
            while (urls.find()) {
                original = original.replace(urls.group(), " ");
                output = output.replace(urls.group(), " ");
            }
            StringBuilder foreign = new StringBuilder();
            for (int codePoint : output.codePoints().toArray()) {
                UnicodeScript script = UnicodeScript.of(codePoint);
                if (script != UnicodeScript.COMMON && script != UnicodeScript.INHERITED
                    && !isTargetScript(script, language)) {
                    foreign.appendCodePoint(codePoint);
                } else {
                    requireOriginalFragment(original, foreign);
                }
            }
            requireOriginalFragment(original, foreign);
        }
    }

    private static void requireOriginalFragment(String original, StringBuilder foreign) {
        // 원문의 고유명사는 허용하되, 해당 문자가 원문에 있다는 이유로 다른 문구까지 허용하지 않는다.
        if (!foreign.isEmpty() && !original.contains(foreign)) {
            throw new TranslationFailureException(TranslationReason.INVALID_TRANSLATION, null);
        }
        foreign.setLength(0);
    }

    private static boolean isTargetScript(UnicodeScript script, SupportedLanguage language) {
        return switch (language) {
            case KOREAN -> script == UnicodeScript.HANGUL;
            case ENGLISH, UZBEK -> script == UnicodeScript.LATIN;
            case SIMPLIFIED_CHINESE -> script == UnicodeScript.HAN;
            case MONGOLIAN -> script == UnicodeScript.CYRILLIC;
            case BURMESE -> script == UnicodeScript.MYANMAR;
            case JAPANESE -> script == UnicodeScript.HAN || script == UnicodeScript.HIRAGANA
                || script == UnicodeScript.KATAKANA;
        };
    }
}
