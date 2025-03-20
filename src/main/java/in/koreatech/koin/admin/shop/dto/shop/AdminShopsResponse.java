package in.koreatech.koin.admin.shop.dto.shop;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopsResponse(
    @Schema(description = "총 상점의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {

    public static AdminShopsResponse of(Page<Shop> pagedResult, Criteria criteria) {
        return new AdminShopsResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopResponse(
        @Schema(description = "속해있는 상점 카테고리 이름 리스트", example = "[\"string\"]", requiredMode = NOT_REQUIRED)
        List<String> categoryNames,

        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이름", example = "홉스", requiredMode = REQUIRED)
        String name,

        @Schema(description = "전화번호", example = "041-000-0000", requiredMode = NOT_REQUIRED)
        String phone,

        @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
        boolean isDeleted
    ) {

        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(
                shop.getShopCategories().stream()
                    .map(shopCategoryMap -> shopCategoryMap.getShopCategory().getName())
                    .toList(),
                shop.getId(),
                shop.getName(),
                shop.getPhone(),
                shop.isDeleted()
            );
        }
    }
}
