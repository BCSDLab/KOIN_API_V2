package in.koreatech.koin.domain.translation.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;

public record TranslationSource(
    TranslationResource resource,
    Map<String, Object> data,
    Map<String, String> texts,
    String version,
    TranslationReason unavailableReason
) {
    public TranslationSource {
        data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
        texts = Collections.unmodifiableMap(new LinkedHashMap<>(texts));
    }

    public static TranslationSource of(TranslationResource resource, Map<String, Object> data,
        Map<String, String> texts, TranslationReason reason) {
        return new TranslationSource(resource, data, texts, TranslationSourceVersion.of(data), reason);
    }
}
