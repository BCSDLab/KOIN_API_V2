package in.koreatech.koin.domain.translation.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.TranslationType;

final class DiningMenuTranslation {
    private static final Pattern BREAKFAST_PRICE = Pattern.compile("(?<![\\p{L}\\d])천\\h*원(?=\\h*의\\h*아침)");
    private static final String WON_PRICE = "₩1,000";
    private static final Pattern TRANSLATED_PRICE = Pattern.compile(
        "(?i)(?:₩|KRW)\\h*1,?000(?!\\d|[.,]\\d)|(?<![\\d.,])1,?000[\\h-]*(?:원|won\\b|KRW\\b)");

    private DiningMenuTranslation() {
    }

    static Map<String, String> inputs(TranslationSource source, SupportedLanguage language) {
        if (!isForeignDining(source, language.getCode())) {
            return source.texts();
        }
        // 숫자로 표기하면 기존 번역 클라이언트가 금액 전체를 한 토큰으로 보호한다.
        Map<String, String> inputs = new LinkedHashMap<>(source.texts());
        inputs.replaceAll((key, text) -> BREAKFAST_PRICE.matcher(text).replaceAll(WON_PRICE));
        return inputs;
    }

    static boolean hasIncorrectPrice(TranslationSource source, String language, Map<String, Object> translated) {
        if (!isForeignDining(source, language) || !(source.data().get("menu") instanceof List<?> originalMenu)) {
            return false;
        }
        List<?> translatedMenu = translated != null && translated.get("menu") instanceof List<?> menu ? menu : List.of();
        for (int i = 0; i < originalMenu.size(); i++) {
            if (originalMenu.get(i) instanceof String item && BREAKFAST_PRICE.matcher(item).find()) {
                if (i >= translatedMenu.size() || !(translatedMenu.get(i) instanceof String text)
                    || !TRANSLATED_PRICE.matcher(text).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isForeignDining(TranslationSource source, String language) {
        return source.resource().type() == TranslationType.DINING && !SupportedLanguage.KOREAN.getCode().equals(language);
    }
}
