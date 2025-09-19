package in.koreatech.koin.domain.order.shop.model.readmodel;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderableShopEvent(
    @Schema(description = "주문 가능 상점 ID")
    Integer orderableShopId,

    @Schema(description = "상점 ID")
    Integer shopId,

    @Schema(description = "상점 이름")
    String shopName,

    @Schema(description = "이벤트 ID")
    Integer eventId,

    @Schema(description = "이벤트 제목")
    String title,

    @Schema(description = "이벤트 내용")
    String content,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "시작일")
    LocalDate startDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "종료일")
    LocalDate endDate
) {

}
