package in.koreatech.koin.domain.language.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.springframework.core.io.ClassPathResource;

import in.koreatech.koin.domain.language.model.SupportedLanguage;

/** 화면의 고정 표시 문구. 원본 코드와 이름을 변경하지 않는다. */
public final class ContentMessageCatalog {
    private static final Map<SupportedLanguage, Map<String, String>> MESSAGES = load("content");
    private static final Map<SupportedLanguage, Map<String, String>> OFFICIAL_NAMES = load("official-names");

    private ContentMessageCatalog() {
    }

    public static String text(SupportedLanguage language, String key, String fallback) {
        return messages(language).getOrDefault(key, fallback);
    }

    public static Map<String, String> messages(SupportedLanguage language) {
        return MESSAGES.get(language == null ? SupportedLanguage.DEFAULT : language);
    }

    static String officialName(SupportedLanguage language, String key, String fallback) {
        return OFFICIAL_NAMES.get(language == null ? SupportedLanguage.DEFAULT : language).getOrDefault(key, fallback);
    }

    private static Map<SupportedLanguage, Map<String, String>> load(String directory) {
        Map<SupportedLanguage, Map<String, String>> result = new EnumMap<>(SupportedLanguage.class);
        for (SupportedLanguage language : SupportedLanguage.values()) {
            String path = "i18n/" + directory + "/" + language.getCode() + ".properties";
            try (var reader = new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                Map<String, String> messages = new TreeMap<>();
                properties.forEach((key, value) -> messages.put(key.toString(), value.toString()));
                result.put(language, Map.copyOf(messages));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load content messages: " + path, e);
            }
        }
        return Map.copyOf(result);
    }
}
