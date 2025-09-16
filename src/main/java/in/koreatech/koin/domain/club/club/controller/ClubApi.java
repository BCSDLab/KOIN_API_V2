package in.koreatech.koin.domain.club.club.controller;

import in.koreatech.koin.domain.club.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.club.enums.ClubSortType;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

@Tag(name = "(Normal) Club: 동아리", description = "동아리 정보를 관리한다")
@RequestMapping("/clubs")
public interface ClubApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 생성을 요청한다")
    @PostMapping
    ResponseEntity<Void> createClubRequest(
        @RequestBody @Valid ClubCreateRequest clubCreateRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 정보를 수정한다")
    @PutMapping("/{clubId}")
    ResponseEntity<ClubResponse> updateClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 상세정보를 수정한다")
    @PutMapping("/{clubId}/introduction")
    ResponseEntity<ClubResponse> updateClubIntroduction(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubIntroductionUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리를 상세조회한다")
    @PostMapping("/{clubId}")
    ResponseEntity<ClubResponse> getClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리를 조회한다", description = """
        - categoryId 값이 없으면 카테고리 구별없이 전체조회가 됩니다.
        - query에 내용을 넣으면 검색이 됩니다.
        - sortType의 기본값은 CREATED_AT_ASC (동아리 생성 순) 입니다.
        - isRecruiting의 기본값은 false 입니다.
        - RECRUITMENT_UPDATED_DESC, RECRUITING_DEADLINE_ASC은 isRecruiting가 true 경우에만 사용할 수 있습니다.
        
        ### 정렬 파라미터
        - NONE : 동아리 생성 순 (향후 정렬 조건 삭제)
        - CREATED_AT_ASC : 동아리 생성 순
        - HITS_DESC : 동아리 조회 순
        - RECRUITMENT_UPDATED_DESC : 동아리 모집 글 생성(수정)순
        - RECRUITING_DEADLINE_ASC : 동아리 모집 마감 순 (모집 마감 짧은 순 -> 상시 모집)
        
        ### 반환 정보
          - recruitmentInfo : 동아리 모집 정보를 담고 있습니다.
            - status : 동아리 모집 상태입니다.
              - NONE : 동아리 모집 글이 없는 상태입니다.
              - RECRUITING : 동아리 모집 중인 상태입니다.
              - CLOSED : 동아리 모집 글이 있는 상태에서 모집 마감된 상태입니다.
              - ALWAYS : 동아리 모집 글이 있는 상태에서 상시 모집중인 상태입니다.
            - Dday : 동아리 모집 디데이 입니다.
              - RECRUITING인 상태에서는 정수로 남은 일자가 내려갑니다.
              - 이외의 상태에서는 null로 내려갑니다.
        """)
    @GetMapping
    ResponseEntity<ClubsByCategoryResponse> getClubs(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false, defaultValue = "false") Boolean isRecruiting,
        @RequestParam(required = false, defaultValue = "CREATED_AT_ASC") ClubSortType sortType,
        @RequestParam(required = false, defaultValue = "") String query,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 검색어에 따른 연관검색어 조회",
        description = """
            ### 접두어 연관검색어 조회
            - 검색어를 접두어로 가지는 모든 동아리명을 조회합니다.
            - ex) B -> BCSD, BASIA
            - 결과물로는 해당 동아리 ID와 동아리명을 반환합니다.
            - 최대 5개까지 반환합니다.
            - 공백, 대소문자 구분X
            - 클라측에서 입력지연(디바운스) 해주셔야 합니다. (무분별한 API 호출 방지)
            """
    )
    @GetMapping("/search/related")
    ResponseEntity<ClubRelatedKeywordResponse> getRelatedClubs(
        @RequestParam(required = false, defaultValue = "") String query
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "인기 동아리를 조회한다")
    @GetMapping("/hot")
    ResponseEntity<ClubHotResponse> getHotClub();
}
