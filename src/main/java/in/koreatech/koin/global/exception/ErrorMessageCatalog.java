package in.koreatech.koin.global.exception;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.http.HttpServletRequest;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.global.code.ApiResponseCode;

/** Response-only localization. Clients send their screen language; authentication is never queried here. */
public final class ErrorMessageCatalog {
    private static final Map<SupportedLanguage, Properties> MESSAGES = loadMessages();

    private ErrorMessageCatalog() {}

    public static String message(HttpServletRequest request, ApiResponseCode code) {
        return message(request, code.getCode(), code.getHttpStatus().value(), code.getMessage());
    }

    public static String message(HttpServletRequest request, String code, int status, String original) {
        SupportedLanguage language = resolveLanguage(request);
        if (language == SupportedLanguage.KOREAN) {
            return original;
        }
        Properties messages = MESSAGES.get(language);
        return messages.getProperty("error." + code, httpMessage(messages, status));
    }

    public static String fallback(HttpServletRequest request, int status, String original) {
        SupportedLanguage language = resolveLanguage(request);
        return language == SupportedLanguage.KOREAN ? original : httpMessage(MESSAGES.get(language), status);
    }

    public static String validation(HttpServletRequest request, String constraint, String original) {
        SupportedLanguage language = resolveLanguage(request);
        if (language == SupportedLanguage.KOREAN) {
            return original;
        }
        Properties messages = MESSAGES.get(language);
        return messages.getProperty("validation." + constraint, messages.getProperty("validation.invalid"));
    }

    public static SupportedLanguage resolveLanguage(HttpServletRequest request) {
        if (request == null) {
            return SupportedLanguage.KOREAN;
        }
        String selected = request.getParameter("language");
        if (selected != null && !selected.isBlank()) {
            return match(selected).orElse(SupportedLanguage.KOREAN);
        }
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) {
            return SupportedLanguage.KOREAN;
        }
        try {
            for (Locale.LanguageRange range : Locale.LanguageRange.parse(header)) {
                if (range.getWeight() > 0) {
                    var language = match(range.getRange());
                    if (language.isPresent()) {
                        return language.get();
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Invalid client language headers must not replace the original application error.
        }
        return SupportedLanguage.KOREAN;
    }

    private static java.util.Optional<SupportedLanguage> match(String code) {
        String normalized = code.trim();
        return Arrays.stream(SupportedLanguage.values())
            .filter(language -> language.getCode().equalsIgnoreCase(normalized)
                || language.getCode().split("-")[0].equalsIgnoreCase(normalized))
            .findFirst();
    }

    private static String httpMessage(Properties messages, int status) {
        return messages.getProperty("http." + status,
            messages.getProperty(status >= 500 ? "http.500" : "http.400"));
    }

    private static Map<SupportedLanguage, Properties> loadMessages() {
        Map<SupportedLanguage, Properties> result = new EnumMap<>(SupportedLanguage.class);
        for (SupportedLanguage language : SupportedLanguage.values()) {
            String path = "/i18n/errors/errors_" + language.getCode() + ".properties";
            Properties properties = new Properties();
            try (var input = ErrorMessageCatalog.class.getResourceAsStream(path)) {
                if (input == null) {
                    throw new IllegalStateException("Missing error message catalog: " + language.getCode());
                }
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read error message catalog: " + language.getCode(), e);
            }
            result.put(language, properties);
        }
        return Map.copyOf(result);
    }
}
