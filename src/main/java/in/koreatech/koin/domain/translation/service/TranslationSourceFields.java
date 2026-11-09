package in.koreatech.koin.domain.translation.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 원문 응답과 번역 로더가 같은 필드와 null 값을 해시에 사용한다. */
public final class TranslationSourceFields {
    private TranslationSourceFields() {
    }

    public static Map<String, Object> summary(List<Map<String, Object>> items) {
        return Map.of("items", items);
    }

    public static Map<String, Object> lostItem(String foundPlace, String content) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("found_place", foundPlace);
        fields.put("content", content);
        return fields;
    }

    public static Map<String, Object> recruitment(String title, String description, String qualification) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("title", title);
        fields.put("description", description);
        fields.put("qualification", qualification);
        return fields;
    }

    public static Map<String, Object> message(String content) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("content", content);
        return fields;
    }
}
