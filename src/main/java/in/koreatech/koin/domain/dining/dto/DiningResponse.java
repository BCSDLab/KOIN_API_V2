package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "총각김치", "생야채샐러드&D", "누룽지탕"]""")
    List<String> menu,

    @Schema(description = "이미지 URL", example = "https://stage.koreatech.in/image.jpg")
    String imageUrl,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 일자", example = "2024-03-15 14:02:48")
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최신화 일자", example = "2024-03-15 14:02:48")
    LocalDateTime updatedAt,

    @Schema(description = "품절 여부", example = "true")
    Boolean soldOut
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
            toListMenus(dining.getMenu()),
            dining.getImageUrl(),
            dining.getCreatedAt(),
            dining.getUpdatedAt(),
            dining.getSoldOut()
        );
    }

    public static List<String> toListMenus(String menu) {
        menu = menu.substring(1, menu.length() - 1);
        return Stream.of(menu.split(","))
            .map(str -> str.trim().replaceAll("\"", ""))
            .collect(Collectors.toList());
    }
}
