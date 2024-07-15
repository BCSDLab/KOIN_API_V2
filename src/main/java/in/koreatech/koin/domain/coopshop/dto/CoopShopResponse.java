package in.koreatech.koin.domain.coopshop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CoopShopResponse(
    @Schema(example = "1", description = "생협 매장 고유 id", requiredMode = REQUIRED)
    Integer id,

    @Schema(example = "세탁소", description = "생협 매장 이름", requiredMode = REQUIRED)
    String name,

    @Schema(description = "요일별 생협 매장 운영시간")
    List<InnerCoopOpens> opens,

    @Schema(example = "041-560-1234", description = "생협 매장 연락처", requiredMode = REQUIRED)
    String phone,

    @Schema(example = "학생식당 2층", description = "생협 매장 위치", requiredMode = REQUIRED)
    String location,

    @Schema(example = "공휴일 휴무", description = "생협 매장 특이사항")
    String remarks
) {

    public static CoopShopResponse from(CoopShop coopShop) {
        return new CoopShopResponse(
            coopShop.getId(),
            coopShop.getName(),
            coopShop.getCoopOpens().stream()
                .map(InnerCoopOpens::from)
                .toList(),
            coopShop.getPhone(),
            coopShop.getLocation(),
            coopShop.getRemarks()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCoopOpens(

        @Schema(example = "평일", description = "생협 매장 운영시간 요일", requiredMode = REQUIRED)
        String dayOfWeek,

        @Schema(example = "아침", description = "생협 매장 운영시간 타입", requiredMode = REQUIRED)
        String type,

        @Schema(example = "09:00", description = "생협 매장 오픈 시간", requiredMode = REQUIRED)
        String openTime,

        @Schema(example = "18:00", description = "생협 매장 마감 시간", requiredMode = REQUIRED)
        String closeTime
    ) {

        public static InnerCoopOpens from(CoopOpen coopOpen) {
            return new InnerCoopOpens(
                coopOpen.getDayOfWeek(),
                coopOpen.getType(),
                coopOpen.getOpenTime(),
                coopOpen.getCloseTime()
            );
        }
    }
}
