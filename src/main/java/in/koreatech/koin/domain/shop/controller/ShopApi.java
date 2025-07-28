package in.koreatech.koin.domain.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import in.koreatech.koin.domain.shop.dto.search.response.RelatedKeywordResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV2;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV3;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "(Normal) Shop: 상점", description = "상점 정보를 관리한다")
public interface ShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점 조회")
    @GetMapping("/shops/{id}")
    ResponseEntity<ShopResponse> getShopById(
        @Parameter(in = PATH) @PathVariable Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 상점 조회")
    @GetMapping("/shops")
    ResponseEntity<ShopsResponse> getShops();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 상점 카테고리 조회")
    @GetMapping("/shops/categories")
    ResponseEntity<ShopCategoriesResponse> getShopsCategories();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "정렬, 필터가 있는 모든 상점 조회")
    @GetMapping("/v2/shops")
    ResponseEntity<ShopsResponseV2> getShopsV2(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteria sortBy,
        @RequestParam(name = "filter") List<ShopsFilterCriteria> shopsFilterCriterias,
        @RequestParam(name = "query", required = false) String query
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "정렬, 필터가 있는 모든 상점 조회 V3", description = """
        ### 주변 상점 조회 V3
        - **V2에서 달라진 점**
            - 이미지 응답값 추가
            - 주문 가능 상점인 경우, 응답값에서 제외
        """
    )
    @GetMapping("/v3/shops")
    ResponseEntity<ShopsResponseV3> getShopsV3(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteriaV3 sortBy,
        @RequestParam(name = "filter", required = false) List<ShopsFilterCriteriaV3> shopsFilterCriterias,
        @RequestParam(name = "query", required = false, defaultValue = "") String query
    );

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            }
    )
    @Operation(
            summary = "주변상점 검색어에 따른 연관검색어 조회",
            description = """
            ### 검색어와 유사한 이름의 음식을 조회
            - 검색어와 관련된 음식의 이름을 조회합니다. ex) 김 → 김치찌개, 김치짜글이
            - 이때 shopIds 필드는 해당 음식과 관련된 상점의 ID를 반환합니다.(관련된 = 해당 음식 이름의 메뉴를 판매중 or 상점에 해당 음식 이름이 포함)  
            - 이 경우 음식과 관련된 것으로 조회하는 것이기 때문에 shopId는 null입니다.
            ### 검색어와 유사한 이름의 상점명을 가진 상점 조회 
            - 검색어와 유사한 상점 이름이 조회됩니다. ex) 즐 → 즐겨먹기
            - 이 경우 shopIds는 빈 배열, shopId는 해당 상점의 ID를 값으로 가집니다.
            
            **shopId가 null인 경우: 연관검색어가 음식 이름인 경우**
            **shopId가 이 아닌 경우: 연관검색어가 상점 이름인 경우**
            """
    )
    @GetMapping("/shops/search/related/{query}")
    ResponseEntity<RelatedKeywordResponse> getRelatedKeyword(
            @PathVariable(name = "query") String query
    );

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            }
    )
    @Operation(summary = "전화하기 리뷰 유도 푸시알림 요청")
    @PostMapping("/shops/{shopId}/call-notification")
    ResponseEntity<Void> createCallNotification(
            @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
            @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer studentId
    );
}
