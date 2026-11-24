package in.koreatech.koin.domain.policy.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.policy.dto.PolicyResponse;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Service
public class PolicyService {
    private static final Set<String> TYPES = Set.of("terms", "privacy");
    private final Map<String, JsonNode> documents;

    public PolicyService(ObjectMapper objectMapper) {
        Map<String, JsonNode> loaded = new HashMap<>();
        for (String type : TYPES) {
            for (SupportedLanguage language : SupportedLanguage.values()) {
                String key = language.getCode() + "/" + type;
                var resource = new ClassPathResource("i18n/policies/" + key + ".json");
                boolean original = language == SupportedLanguage.KOREAN;
                if (!resource.exists() && !original) {
                    continue;
                }
                try (var input = resource.getInputStream()) {
                    JsonNode document = objectMapper.readTree(input);
                    // Korean is copied from KOIN_WEB_RECODE/src/static/policy.ts, not newly reviewed.
                    // Foreign documents are served only after a reviewed:true resource is supplied.
                    if (!original && !document.path("reviewed").isBoolean()) {
                        continue;
                    }
                    if (!original && !document.path("reviewed").booleanValue()) {
                        continue;
                    }
                    JsonNode sections = document.path("sections");
                    if (!sections.isArray() || sections.isEmpty()) {
                        throw new IllegalStateException("Policy sections missing: " + key);
                    }
                    loaded.put(key, sections);
                } catch (IOException exception) {
                    throw new IllegalStateException("Cannot load policy: " + key, exception);
                }
            }
        }
        documents = Map.copyOf(loaded);
    }

    public PolicyResponse get(String type, String languageCode) {
        if (!TYPES.contains(type)) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        SupportedLanguage requested = SupportedLanguage.fromCode(languageCode);
        String language = requested.getCode();
        JsonNode sections = documents.get(language + "/" + type);
        boolean fallback = sections == null;
        if (fallback) {
            language = SupportedLanguage.KOREAN.getCode();
            sections = documents.get(language + "/" + type);
        }
        return new PolicyResponse(type, requested.getCode(), language, fallback, sections.deepCopy());
    }
}
