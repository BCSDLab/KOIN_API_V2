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

    public record Message(String content) {
    }
}
