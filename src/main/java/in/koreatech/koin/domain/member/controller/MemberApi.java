package in.koreatech.koin.domain.member.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.member.dto.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Member : BCSDLab 회원", description = "BCSDLab 회원 정보를 관리한다")
public interface MemberApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200")
        }
    )
    @Operation(summary = "회원 목록 조회")
    @GetMapping("/members")
    ResponseEntity<List<MemberResponse>> getMembers();
}
