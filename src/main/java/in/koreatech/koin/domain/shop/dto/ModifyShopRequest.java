package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ModifyShopRequest(
    @Schema(example = "충청남도 천안시 동남구 병천면", description = "주소")
    String address,

    @Schema(example = "[1, 2]", description = "상점 카테고리 고유 id 리스트")
    List<Long> categoryIds,

    @Schema(example = "true", description = "배달 가능 여부")
    Boolean delivery,

    @Schema(example = "1000", description = "배달비")
    Integer deliveryPrice,

    @Schema(example = "string", description = "설명")
    String description,

    @Schema(example = "string", description = "이미지 URL 리스트")
    List<String> imageUrls,

    @Schema(example = "수신반점", description = "이름")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간")
    List<InnerShopOpen> open,

    @Schema(example = "true", description = "계좌 이체 가능 여부")
    Boolean payBank,

    @Schema(example = "false", description = "카드 계산 가능 여부")
    Boolean payCard,

    @Schema(example = "041-000-0000", description = "전화번호")
    String phone
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(example = "MONDAY", description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """)
        String dayOfWeek,

        @Schema(example = "false", description = "휴무 여부")
        Boolean closed,

        @Schema(example = "02:00", description = "오픈 시간")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @Schema(example = "16:00", description = "마감 시간")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime
    ) {
        public ShopOpen toEntity(Shop shop) {
            return ShopOpen.builder()
                .shop(shop)
                .openTime(openTime)
                .closed(closed)
                .closeTime(closeTime)
                .dayOfWeek(dayOfWeek)
                .build();
        }
    }
}
