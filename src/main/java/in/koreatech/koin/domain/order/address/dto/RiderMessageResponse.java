package in.koreatech.koin.domain.order.address.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.address.model.RiderMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * record 관련 레디스 캐시 직렬화 문제가 있어서 class 로 구현
 */
@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class RiderMessageResponse {

    @Schema(description = "요청 사항 수", example = "5")
    private final Integer count;

    @Schema(description = "배달 기사 요청 사항 목록")
    private final List<InnerRiderMessageResponse> contents;

    @JsonCreator
    public RiderMessageResponse(
        @JsonProperty("count") Integer count,
        @JsonProperty("contents") List<InnerRiderMessageResponse> contents
    ) {
        this.count = count;
        this.contents = contents;
    }

    @Getter
    @JsonNaming(SnakeCaseStrategy.class)
    public static class InnerRiderMessageResponse{

        @Schema(description = "배달 기사 요청 사항", example = "문 앞에 놔주세요 (노크해주세요)")
        private final String content;

        @JsonCreator
        public InnerRiderMessageResponse(
            @JsonProperty("content") String content
        ) {
            this.content = content;
        }

        public static InnerRiderMessageResponse from(RiderMessage riderMessage) {
            return new InnerRiderMessageResponse(riderMessage.getContent());
        }
    }

    public static RiderMessageResponse from(List<RiderMessage> riderMessages) {
        return new RiderMessageResponse(riderMessages.size(),
            riderMessages.stream().map(InnerRiderMessageResponse::from).toList());
    }
}
