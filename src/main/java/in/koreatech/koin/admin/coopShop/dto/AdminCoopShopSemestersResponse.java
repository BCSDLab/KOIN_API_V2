package in.koreatech.koin.admin.coopShop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCoopShopSemestersResponse(

    @Schema(description = "조건에 해당하는 총 학기 수", example = "20", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 학기 중 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 학기를 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    List<InnerSemester> coopShopSemesters
) {

    record InnerSemester(
        Integer id,
        String semester,
        String term
    ) {

        public static InnerSemester from(CoopShopSemester coopShopSemester) {
            return new InnerSemester(
                coopShopSemester.getId(),
                coopShopSemester.getSemester(),
                coopShopSemester.getTerm()
            );
        }
    }

    public static AdminCoopShopSemestersResponse of(Page<CoopShopSemester> pagedResult, Criteria criteria) {
        return new AdminCoopShopSemestersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(InnerSemester::from)
                .toList()
        );
    }
}
