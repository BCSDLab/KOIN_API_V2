package in.koreatech.koin.admin.benefit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.benefit.dto.AdminBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminDeleteShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminSearchBenefitShopsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) ShopBenefit: 상점 혜택", description = "어드민 상점 혜택 정보를 관리한다")
@RequestMapping("/admin/benefit")
public interface AdminBenefitApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 혜택 카테고리를 모두 조회한다.")
    @GetMapping("/categories")
    ResponseEntity<AdminBenefitCategoryResponse> getBenefitCategories();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "혜택 카테고리를 추가한다.")
    @PostMapping("/categories")
    ResponseEntity<AdminCreateBenefitCategoryResponse> createBenefitCategory(
        @RequestBody AdminCreateBenefitCategoryRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "혜택 카테고리를 삭제한다.")
    @DeleteMapping("/categories/{id}")
    ResponseEntity<Void> deleteBenefitCategory(
        @PathVariable("id") Integer categoryId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 혜택 카테고리에 속하는 상점을 모두 조회한다.")
    @GetMapping("/{id}/shops")
    ResponseEntity<AdminBenefitShopsResponse> getBenefitShops(
        @PathVariable("id") Integer benefitId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 혜택을 제공하는 상점을 추가한다.")
    @PostMapping("/{id}/shops")
    ResponseEntity<AdminCreateBenefitShopsResponse> createBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminCreateBenefitShopsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 혜택을 제공하는 상점을 삭제한다.")
    @DeleteMapping("/{id}/shops")
    ResponseEntity<Void> deleteBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminDeleteShopsRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "혜택 상점을 추가하기 위해 상점을 검색한다.")
    @GetMapping("/{id}/shops/search")
    ResponseEntity<AdminSearchBenefitShopsResponse> searchShops(
        @PathVariable("id") Integer benefitId,
        @RequestParam("search_keyword") String searchKeyword
    );
}
