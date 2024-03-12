package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.Dining;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningResponse(

    @Schema(description = "메뉴 고유 ID", example = "1")
    Long id,

    @Schema(description = "식단 제공 날짜", example = "2024-03-11")
    String date,

    @Schema(description = "식사 시간", example = "LUNCH")
    String type,

    @Schema(description = "식단 제공 장소", example = "A코스")
    String place,

    @Schema(description = "카드 가격", example = "5000")
    Integer priceCard,

    @Schema(description = "현금 가격", example = "5000")
    Integer priceCash,

    @Schema(description = "칼로리", example = "790")
    Integer kcal,

    @Schema(description = "식단", example = """
        ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "오이소박이양념무침", "총각김치", "생야채샐러드&D", "누룽지탕"]
        """)
    String menu
) {
    public static DiningResponse from(Dining dining) {
        return new DiningResponse(
            dining.getId(),
            dining.getDate(),
            dining.getType(),
            dining.getPlace(),
            dining.getPriceCard(),
            dining.getPriceCash(),
            dining.getKcal(),
            dining.getMenu()
        );
    }
}
