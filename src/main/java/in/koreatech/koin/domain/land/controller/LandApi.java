package in.koreatech.koin.domain.land.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.dto.LandsGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Land: 복덕방", description = "복덕방 정보를 관리한다")
public interface LandApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 목록 조회", description = """
        ### 복덕방 목록 조회
        - `query`가 없거나 공백뿐이면 전체 목록을 반환합니다.
        - `query`가 있으면 건물명 부분 일치로 검색합니다.
            - 띄어쓰기와 대소문자는 구분하지 않습니다. ex) "별밭 하우스", "밭하우" => "별밭하우스"
            - 삭제된 건물은 검색 결과에서 제외합니다.
            - 일치하는 건물이 없으면 빈 목록을 반환합니다.
        """)
    @GetMapping("/lands")
    ResponseEntity<LandsGroupResponse> getLands(
        @Parameter(in = QUERY, description = "건물명 검색어 (선택)")
        @RequestParam(required = false, defaultValue = "") String query
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 단건 조회")
    @GetMapping("/lands/{id}")
    ResponseEntity<LandResponse> getLand(
        @Parameter(in = PATH) @PathVariable Integer id
    );
}
