package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminNewOwnersResponse(
    @Schema(description = "조건에 해당하는 총 사장님의 수", example = "57", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "조건에 해당하는 사장님중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 사장님들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "사장님 리스트", requiredMode = REQUIRED)
    List<InnerNewOwnerResponse> owners
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerNewOwnerResponse(
        @Schema(description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이메일", requiredMode = REQUIRED)
        String email,

        @Schema(description = "이름")
        String name,

        @Schema(description = "전화번호")
        String phoneNumber,

        @Schema(description = "요청한 상점ID")
        Integer shopId,

        @Schema(description = "요청한 상점명")
        String shopName,

        @Schema(description = "가입 신청 일자", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
    ) {
    }
}
