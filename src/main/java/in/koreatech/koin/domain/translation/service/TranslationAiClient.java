package in.koreatech.koin.domain.translation.service;

import java.util.Map;

import in.koreatech.koin.domain.language.model.SupportedLanguage;

public interface TranslationAiClient {
    Map<String, String> translate(Map<String, String> fields, SupportedLanguage targetLanguage);

    String detectLanguage(Map<String, String> fields);
}
