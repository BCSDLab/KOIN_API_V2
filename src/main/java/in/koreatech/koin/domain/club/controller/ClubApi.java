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
import in.koreatech.koin.domain.club.dto.request.CreateClubRequest;
import in.koreatech.koin.domain.club.dto.request.CreateQnaRequest;
import in.koreatech.koin.domain.club.dto.request.UpdateClubIntroductionRequest;
import in.koreatech.koin.domain.club.dto.request.UpdateClubRequest;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.QnasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
        @RequestBody @Valid CreateClubRequest createClubRequest,
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
        @RequestBody UpdateClubRequest request,
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
        @RequestBody UpdateClubIntroductionRequest request,
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
    @Operation(summary = "카테고리를 기준으로 동아리를 조회한다")
    @PostMapping
    ResponseEntity<ClubsByCategoryResponse> getClubByCategory(
        @RequestParam Integer categoryId,
        @RequestParam(required = false) Boolean hitSort
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
    ResponseEntity<QnasResponse> getQnas(
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
        @RequestBody @Valid CreateQnaRequest request,
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
            - 부모 QNA(질문 QNA)인 경우, 자식 QNA까지 모두 삭제
            - 자식 QNA(답변 QNA)인 경우, 그거만 삭제 
            """)
    @DeleteMapping("/{clubId}/qna/{qnaId}")
    ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
