package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminOwnersResponse(
    @Schema(description = "조건에 해당하는 총 사장님의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 사장님중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 사장님들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "사장님 리스트", requiredMode = REQUIRED)
    List<InnerOwnersResponse> owners
) {
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerOwnersResponse(
        @Schema(description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이메일", requiredMode = REQUIRED)
        String email,

        @Schema(description = "이름", requiredMode = NOT_REQUIRED)
        String name,

        @Schema(description = "전화번호", requiredMode = NOT_REQUIRED)
        String phoneNumber,

        @Schema(description = "가입 신청 일자", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
    ) {
        public static InnerOwnersResponse from(Owner owner) {
            return new InnerOwnersResponse(
                owner.getUser().getId(),
                owner.getUser().getEmail(),
                owner.getUser().getName(),
                owner.getUser().getPhoneNumber(),
                owner.getUser().getCreatedAt()
            );
        }
    }

    public static AdminOwnersResponse of(Page<Owner> pagedResult, Criteria criteria) {
        return new AdminOwnersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent().stream()
                .map(InnerOwnersResponse::from)
                .collect(Collectors.toList())
        );
    }
}
