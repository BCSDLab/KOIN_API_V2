package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminNewOwnersResponse(
    @Schema(description = "조건에 해당하는 총 사장님의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

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
    public record InnerNewOwnerResponse(
        @Schema(description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이메일", requiredMode = REQUIRED)
        String email,

        @Schema(description = "이름", requiredMode = NOT_REQUIRED)
        String name,

        @Schema(description = "전화번호", requiredMode = NOT_REQUIRED)
        String phoneNumber,

        @Schema(description = "요청한 상점ID", requiredMode = NOT_REQUIRED)
        Integer shopId,

        @Schema(description = "요청한 상점명", requiredMode = NOT_REQUIRED)
        String shopName,

        @Schema(description = "가입 신청 일자", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
    ) {
        public static InnerNewOwnerResponse from(OwnerIncludingShop ownerIncludingShop) {
            User user = ownerIncludingShop.getOwner().getUser();
            return new InnerNewOwnerResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                ownerIncludingShop.getShop_id(),
                ownerIncludingShop.getShop_name(),
                user.getCreatedAt()
            );
        }
    }

    public static AdminNewOwnersResponse of(List<OwnerIncludingShop> ownerIncludingShops, Page pagedResult, Criteria criteria) {
        return new AdminNewOwnersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            ownerIncludingShops.stream()
                .map(InnerNewOwnerResponse::from)
                .collect(Collectors.toList())
        );
    }
}
