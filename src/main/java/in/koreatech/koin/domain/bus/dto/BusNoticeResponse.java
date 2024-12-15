package in.koreatech.koin.domain.bus.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BusNoticeResponse(
        @Schema(description = "공지글 번호", example = "17153", requiredMode = NOT_REQUIRED)
        Integer id,

        @Schema(description = "공지글 제목", example = "[긴급][총무팀]2024.11.27.(수, 오늘) 천안, 청주 야간 셔틀 20시 지연운행", requiredMode = NOT_REQUIRED)
        String title
) {

    public static BusNoticeResponse of(Integer id, String title) {
        return new BusNoticeResponse(id, title);
    }
}
