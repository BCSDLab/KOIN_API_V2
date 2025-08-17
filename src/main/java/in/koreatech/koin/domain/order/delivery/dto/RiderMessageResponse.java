package in.koreatech.koin.domain.order.delivery.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.delivery.model.RiderMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record RiderMessageResponse(
    @Schema(description = "요청 사항 수", example = "5")
    Integer count,

    @Schema(description = "배달 기사 요청 사항 목록")
    List<InnerRiderMessageResponse> contents)
{

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerRiderMessageResponse(
        @Schema(description = "배달 기사 요청 사항", example = "문 앞에 놔주세요 (노크해주세요)")
        String content
    ) {
        public static InnerRiderMessageResponse from(RiderMessage riderMessage) {
            return new InnerRiderMessageResponse(riderMessage.getContent());
        }
    }

    public static RiderMessageResponse from(List<RiderMessage> riderMessages) {
        return new RiderMessageResponse(riderMessages.size(),
            riderMessages.stream().map(InnerRiderMessageResponse::from).toList());
    }
}
