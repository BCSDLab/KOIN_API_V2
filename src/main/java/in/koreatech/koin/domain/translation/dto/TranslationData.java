package in.koreatech.koin.domain.translation.dto;

import java.util.List;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

public final class TranslationData {
    private TranslationData() {
    }

    public record Summary(List<SummaryItem> items) {
    }

    public record SummaryItem(
        @Schema(description = "원문의 아이콘. 번역하지 않습니다.", example = "📅") String icon,
        @Schema(description = "번역한 요약 문장. 한국어 생성의 100자 상한은 적용하지 않습니다.") String text
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record LostItem(String foundPlace, @Schema(nullable = true) String content) {
    }

    public record Recruitment(String title, String description, @Schema(nullable = true) String qualification) {
    }

    public record Dining(List<String> menu) {
    }

    public record Shop(@Schema(nullable = true) String description) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ShopInfo(@Schema(nullable = true) String introduction, @Schema(nullable = true) String notice,
        @Schema(nullable = true) String addressDetail) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ShopMenu(String name, @Schema(nullable = true) String description,
        @Schema(description = "원문 순서의 옵션명과 가격. 단일 메뉴는 null입니다.", nullable = true)
        List<ShopMenuOptionPrice> optionPrices) {
    }

    public record ShopMenuOptionPrice(@Schema(nullable = true) String option,
        @Schema(description = "원문 가격. 번역하지 않습니다.") Integer price) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record OrderableShopMenu(String name, @Schema(nullable = true) String description,
        List<NamedItem> prices, List<MenuOptionGroup> optionGroups) {
    }

    public record NamedItem(@Schema(description = "원문 항목 ID. 변경하지 않습니다.") Integer id,
        @Schema(nullable = true) String name) {
    }

    public record MenuOptionGroup(Integer id, String name, @Schema(nullable = true) String description,
        List<NamedItem> options) {
    }

    public record Name(String name) {
    }

    public record Message(String content) {
    }
}
