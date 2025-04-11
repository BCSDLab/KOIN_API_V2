package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerActiveChangeRequest;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerCreateRequest;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerModifyRequest;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerPriorityChangeRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Banner: 배너", description = "어드민 배너 정보를 관리한다")
@RequestMapping("/admin/banners")
public interface AdminBannerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 배너 정보를 조회한다.")
    @GetMapping("/{id}")
    ResponseEntity<AdminBannerResponse> getBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
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
    @Operation(summary = "배너 정보을 페이지네이션으로 조회한다.", description = """
        banner_category_name으로 메인 모달, 모바일 가로 배너, 웹 가로 배너, 웹 세로 배너의 값을 주시면 됩니다.
        활성화 필터를 사용하지 않을 때는 is_active 값을 안 주셔도 됩니다.
        """)
    @GetMapping
    ResponseEntity<AdminBannersResponse> getBanners(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_active", required = false) Boolean isActive,
        @RequestParam(name = "banner_category_name") String bannerCategoryName,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "배너를 생성한다", description = """
        - 웹 배포 여부
            - 웹 배포여부가 비활성화된 경우, 리다이렉션 링크는 존재하면 안됩니다.
        - 모바일 배포 여부
            - 모바일 배포가 활성화된 경우, 리다이렉션 링크와 최소 버전은 모두 존재하거나 모두 없어야 합니다.
            - 모바일 배포가 비활성화된 경우, 리다이렉션 링크와 최소버전을 설정할 수 없습니다.
        """)
    @PostMapping
    ResponseEntity<Void> createBanner(
        @RequestBody @Valid AdminBannerCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
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
    @Operation(summary = "특정 배너를 삭제한다")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
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
    @Operation(summary = "특정 배너의 우선순위를 조정한다", description = """
        - 배너의 우선순위를 올리고 싶으면 UP, 내리고 싶으면 DOWN 값을 보내주시면 됩니다.
            - is_active가 false인 배너에 해당 API를 호출하면 에러가 발생합니다.
            - is_active가 true인데 priority가 null인 배너에 해당 API를 호출하면 에러가 발생합니다.
            - priority가 0인 배너에 해당 API의 값을 UP으로 호출하면 에러가 발생합니다.
            - priority가 해당 카테고리에서 마지막 값인 배너에 해당 API의 값을 DOWN으로 호출하면 에러가 발생합니다.
        - UP
            - 이전 우선순위를 가진 배너와 우선순위가 변경 됩니다. (명세서 기준 화살표 윗방향)
                - ex. 위에 우선순위2, 지금꺼가 우선순위 3이면 UP 사용시 지금꺼가 우선순위2, 위에꺼가 우선순위3으로 변경됨
        - DOWN
            - 이후 우선순위를 가진 배너와 우선순위가 변경 됩니다. (명세서 기준 화살표 아랫방향)
        """)
    @PatchMapping("/{id}/priority")
    ResponseEntity<Void> changePriority(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminBannerPriorityChangeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
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
    @Operation(summary = "특정 배너의 활성화 상태를 조정한다")
    @PatchMapping("/{id}/active")
    ResponseEntity<Void> changeActive(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminBannerActiveChangeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
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
    @Operation(summary = "특정 배너를 수정한다", description = """
        - 웹 배포 여부
            - 웹 배포여부가 비활성화된 경우, 리다이렉션 링크는 존재하면 안됩니다.
        - 모바일 배포 여부
            - 모바일 배포가 활성화된 경우, 리다이렉션 링크와 최소 버전은 모두 존재하거나 모두 없어야 합니다.
            - 모바일 배포가 비활성화된 경우, 리다이렉션 링크와 최소버전을 설정할 수 없습니다.
        """)
    @PutMapping("/{id}")
    ResponseEntity<Void> modifyBanner(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminBannerModifyRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
