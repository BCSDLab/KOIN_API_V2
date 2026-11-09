package in.koreatech.koin.domain.translation.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuResponse;

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

    public static Map<String, Object> dining(List<String> menu) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("menu", menu);
        return fields;
    }

    public static Map<String, Object> shop(String description) {
        Map<String, Object> fields = new LinkedHashMap<>();
        // 기존 상점 상세 응답이 빈 설명을 "-"로 표시하므로 원문 버전도 표시값과 맞춘다.
        fields.put("description", description == null || description.isBlank() ? "-" : description);
        return fields;
    }

    public static Map<String, Object> shopInfo(String introduction, String notice, String addressDetail) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("introduction", introduction);
        fields.put("notice", notice);
        fields.put("address_detail", addressDetail);
        return fields;
    }

    public static Map<String, Object> shopMenu(String name, String description) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", name);
        fields.put("description", description);
        return fields;
    }

    public static Map<String, Object> shopMenu(String name, String description,
        List<Map<String, Object>> optionPrices) {
        Map<String, Object> fields = shopMenu(name, description);
        fields.put("option_prices", optionPrices);
        return fields;
    }

    public static Map<String, Object> optionPrice(String option, Integer price) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("option", option);
        fields.put("price", price);
        return fields;
    }

    public static Map<String, Object> orderableShopMenu(OrderableShopMenuResponse menu) {
        Map<String, Object> fields = shopMenu(menu.name(), menu.description());
        fields.put("prices", menu.prices().stream().map(price -> identifiedName(price.id(), price.name())).toList());
        fields.put("option_groups", menu.optionGroups().stream().map(group -> {
            Map<String, Object> data = identifiedName(group.id(), group.name());
            data.put("description", group.description());
            data.put("options", group.options().stream()
                .map(option -> identifiedName(option.id(), option.name())).toList());
            return data;
        }).toList());
        return fields;
    }

    private static Map<String, Object> identifiedName(Integer id, String name) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", id);
        fields.put("name", name);
        return fields;
    }

    public static Map<String, Object> name(String name) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", name);
        return fields;
    }

    public static Map<String, Object> message(String content) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("content", content);
        return fields;
    }
}
