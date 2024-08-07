package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.Dining;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningResponse(

    @Schema(description = "메뉴 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "식단 제공 날짜", example = "2024-03-11", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @Schema(description = "식사 시간", example = "LUNCH", requiredMode = REQUIRED)
    String type,

    @Schema(description = "식단 제공 장소", example = "A코스", requiredMode = REQUIRED)
    String place,

    @Schema(description = "카드 가격", example = "5000", requiredMode = NOT_REQUIRED)
    Integer priceCard,

    @Schema(description = "현금 가격", example = "5000", requiredMode = NOT_REQUIRED)
    Integer priceCash,

    @Schema(description = "칼로리", example = "790", requiredMode = NOT_REQUIRED)
    Integer kcal,

    @Schema(description = "식단", example = """
        ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "총각김치", "생야채샐러드&D", "누룽지탕"]
        """, requiredMode = REQUIRED)
    List<String> menu,

    @Schema(description = "이미지 URL", example = "https://stage.koreatech.in/image.jpg", requiredMode = NOT_REQUIRED)
    String imageUrl,

    @Schema(description = "생성 일자", example = "2024-03-15 14:02:48", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "최신화 일자", example = "2024-03-15 14:02:48", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "품절 시각", example = "2024-04-04 23:01:52", requiredMode = NOT_REQUIRED)
    LocalDateTime soldoutAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "메뉴 변경 시각", example = "2024-04-04 23:01:52", requiredMode = NOT_REQUIRED)
    LocalDateTime changedAt,

    @Schema(description = "식단 좋아요 수", example = "1", requiredMode = REQUIRED)
    Integer likes,

    @Schema(description = "식단 좋아요 여부", example = "true", requiredMode = REQUIRED)
    Boolean isLiked
) {

    public static DiningResponse from(Dining dining, Boolean isLiked) {
        return new DiningResponse(
            dining.getId(),
            dining.getDate(),
            dining.getType().name(),
            dining.getPlace(),
            dining.getPriceCard(),
            dining.getPriceCash(),
            dining.getKcal() != null ? dining.getKcal() : 0,
            dining.getMenu(),
            dining.getImageUrl(),
            dining.getCreatedAt(),
            dining.getUpdatedAt(),
            dining.getSoldOut(),
            dining.getIsChanged(),
            dining.getLikes(),
            isLiked
        );
    }
}
