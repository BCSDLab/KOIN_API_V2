package in.koreatech.koin.domain.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.auth.UserId;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    @Operation(summary = "카테고리를 기준으로 동아리를 조회한다", description = """
        - categoryId 값이 없으면 카테고리 구별없이 전체조회가 됩니다.
        - query에 내용을 넣으면 검색이 됩니다
        """)
    @GetMapping
    ResponseEntity<ClubsByCategoryResponse> getClubByCategory(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false, defaultValue = "NONE") ClubSortType sortType,
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

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 좋아요를 누른다")
    @PutMapping("/{clubId}/like")
    ResponseEntity<Void> likeClub(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 좋아요를 취소한다")
    @DeleteMapping("/{clubId}/like/cancel")
    ResponseEntity<Void> likeClubCancel(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @Operation(
        summary = "특정 동아리의 모든 QNA를 조회한다",
        description = """
            - authorId 확인하여 작성자 본인인 경우 삭제 버튼(x) 표시.
            - 닉네임은 존재 시 그대로 반환되며, 없는 경우 student의 익명 닉네임으로 반환.
            - 트리 구조는 대댓글 형태로 재귀적으로 구성됩니다.
            
            ```java
            예시
            댓글 1
            ├── 댓글 1-1
            │   ├── 댓글 1-1-1
            │   │   └── 댓글 1-1-1-1
            │   └── 댓글 1-1-2
            ├── 댓글 1-2
            └── 댓글 1-3
            
            댓글 2
            └── 댓글 2-1
                └── 댓글 2-1-1
            ```
            """
    )
    @GetMapping("/{clubId}/qna")
    ResponseEntity<ClubQnasResponse> getQnas(
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 동아리의 QNA를 생성한다",
        description = """
            사용자의 경우 질문만 가능, 관리자의 경우 답변만 가능
            parentId를 null 요청 시 질문, 질문 QNA의 id를 넣어서 요청하면 답변 형식으로 생성
            """
    )
    @PostMapping("/{clubId}/qna")
    ResponseEntity<Void> createQna(
        @RequestBody @Valid ClubQnaCreateRequest request,
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
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
    @Operation(summary = "특정 동아리의 QNA를 삭제한다",
        description = """
            - 관리자는 모든 QNA 삭제 가능, 그 외에는 본인의 QNA만 삭제 가능
            - 부모 QNA(질문 QNA)인 경우, 답변 QNA까지 모두 삭제
            - 자식 QNA(답변 QNA)인 경우, 답변 QNA만 삭제
            """)
    @DeleteMapping("/{clubId}/qna/{qnaId}")
    ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
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
    @Operation(summary = "동아리 관리자 권한을 위임한다")
    @PutMapping("/empowerment")
    ResponseEntity<Void> empowermentClubManager(
        @RequestBody @Valid ClubManagerEmpowermentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "동아리 모집 생성 성공", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "모집 종료일은 시작일 이후여야 함", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "종료일이 시작일보다 빠른 경우", value = """
                    {
                      "code": "INVALID_RECRUITMENT_PERIOD",
                      "message": "모집 마감일은 모집 시작일 이후여야 합니다.",
                      "errorTraceId": "0c790c6c-e323-40db-ba4b-6e0ab49e9f7d"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "400", description = "상시 모집일 경우 모집 기간이 없어야 함", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "상시 모집인데 기간이 입력된 경우", value = """
                    {
                      "code": "MUST_BE_NULL_RECRUITMENT_PERIOD",
                      "message": "상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(
                responseCode = "400", description = "상시 모집이 아닌데 모집 시작일 또는 종료일이 입력되지 않은 경우", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "상시 모집 아닌 경우 모집 기간 누락", value = """
                        {
                          "code": "REQUIRED_RECRUITMENT_PERIOD",
                          "message": "상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다.",
                          "errorTraceId": "b7f340c2-2d74-4f8e-9c84-94d2eaaa1d44"
                        }
                    """)
            })),
            @ApiResponse(responseCode = "401", description = "동아리 매니저가 아닌 경우 모집글 작성 불가", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "비매니저 사용자가 모집글 작성한 경우", value = """
                    {
                      "code": "",
                      "message": "권한이 없습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 동아리 ID", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "없는 동아리 ID로 요청한 경우", value = """
                    {
                      "code": "NOT_FOUND_CLUB",
                      "message": "동아리가 존재하지 않습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 ID", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "없는 유저 ID로 요청한 경우", value = """
                    {
                      "code": "NOT_FOUND_USER",
                      "message": "해당 사용자를 찾을 수 없습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            }))
        }
    )
    @Operation(summary = "동아리 모집 생성", description = """
        ### 동아리 모집 생성
        - 동아리 모집 생성을 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부의 기본값은 false입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        
        ### 에러 코드(에러 메시지)
        - INVALID_RECRUITMENT_PERIOD (모집 마감일은 모집 시작일 이후여야 합니다.)
        - RECRUITMENT_PERIOD_MUST_BE_NULL (상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다.)
        - RECRUITMENT_PERIOD_REQUIRED (상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다.)
        - NOT_FOUND_CLUB (동아리가 존재하지 않습니다.)
        - NOT_FOUND_USER (해당 사용자를 찾을 수 없습니다.)
        """)
    @PostMapping("/{clubId}/recruitment")
    ResponseEntity<Void> createRecruitment(
        @RequestBody @Valid ClubRecruitmentCreateRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "동아리 모집 생성 성공", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "모집 종료일은 시작일 이후여야 함", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "종료일이 시작일보다 빠른 경우", value = """
                    {
                      "code": "INVALID_RECRUITMENT_PERIOD",
                      "message": "모집 마감일은 모집 시작일 이후여야 합니다.",
                      "errorTraceId": "0c790c6c-e323-40db-ba4b-6e0ab49e9f7d"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "400", description = "상시 모집일 경우 모집 기간이 없어야 함", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "상시 모집인데 기간이 입력된 경우", value = """
                    {
                      "code": "MUST_BE_NULL_RECRUITMENT_PERIOD",
                      "message": "상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(
                responseCode = "400", description = "상시 모집이 아닌데 모집 시작일 또는 종료일이 입력되지 않은 경우", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "상시 모집 아닌 경우 모집 기간 누락", value = """
                        {
                          "code": "REQUIRED_RECRUITMENT_PERIOD",
                          "message": "상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다.",
                          "errorTraceId": "b7f340c2-2d74-4f8e-9c84-94d2eaaa1d44"
                        }
                    """)
            })),
            @ApiResponse(responseCode = "401", description = "동아리 매니저가 아닌 경우 모집글 작성 불가", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "비매니저 사용자가 모집글 작성한 경우", value = """
                    {
                      "code": "",
                      "message": "권한이 없습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 동아리 ID", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "없는 동아리 ID로 요청한 경우", value = """
                    {
                      "code": "NOT_FOUND_CLUB",
                      "message": "동아리가 존재하지 않습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 ID", content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "없는 유저 ID로 요청한 경우", value = """
                    {
                      "code": "NOT_FOUND_USER",
                      "message": "해당 사용자를 찾을 수 없습니다.",
                      "errorTraceId": "e13f4f4a-88a7-44a2-b1b5-2b14f4cdee12"
                    }
                    """)
            }))
        }
    )
    @Operation(summary = "동아리 모집 생성", description = """
        ### 동아리 모집 수정
        - 동아리 모집을 수정 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        
        ### 에러 코드(에러 메시지)
        - INVALID_RECRUITMENT_PERIOD (모집 마감일은 모집 시작일 이후여야 합니다.)
        - RECRUITMENT_PERIOD_MUST_BE_NULL (상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다.)
        - RECRUITMENT_PERIOD_REQUIRED (상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다.)
        - NOT_FOUND_CLUB (동아리가 존재하지 않습니다.)
        - NOT_FOUND_USER (해당 사용자를 찾을 수 없습니다.)
        """)
    @PutMapping("/{clubId}/recruitment")
    ResponseEntity<Void> modifyRecruitment(
        @RequestBody @Valid ClubRecruitmentModifyRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
