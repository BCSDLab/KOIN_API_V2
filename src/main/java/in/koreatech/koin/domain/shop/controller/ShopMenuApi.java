package in.koreatech.koin.domain.shop.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import in.koreatech.koin.domain.shop.dto.menu.response.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.ShopMenuResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "(Normal) ShopMenu: 상점메뉴", description = "상점 메뉴 정보를 관리한다")
public interface ShopMenuApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "메뉴 단건 조회", description = "설정 언어로 자동 표시하려면 translation_source_version으로 POST /shops/{shopId}/menus/{menuId}/translations를 호출합니다. 원문 필드는 유지합니다.")
    @SecurityRequirements
    @GetMapping("/shops/{shopId}/menus/{menuId}")
    ResponseEntity<MenuDetailResponse> findMenu(
        @Parameter(in = PATH) @PathVariable Integer shopId,
        @Parameter(in = PATH) @PathVariable Integer menuId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점의 모든 메뉴 조회", description = "화면에 표시할 메뉴의 translation_source_version과 설정 언어로 메뉴 번역 POST를 호출합니다. 메뉴 분류도 각 분류의 버전으로 별도 번역합니다. 원문 필드는 유지합니다.")
    @SecurityRequirements
    @GetMapping("/shops/{id}/menus")
    ResponseEntity<ShopMenuResponse> findMenus(
        @Parameter(in = PATH) @PathVariable Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "메뉴 카테고리 목록 조회", description = "상점이 작성한 메뉴 분류는 각 translation_source_version과 설정 언어로 POST /shops/{shopId}/menus/categories/{id}/translations를 호출해 표시합니다.")
    @SecurityRequirements
    @GetMapping("/shops/{shopId}/menus/categories")
    ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @Parameter(in = PATH) @PathVariable Integer shopId
    );
}
