package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.community.article.dto.LostItemReportRequest;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Articles: 게시글", description = "게시글 정보를 관리한다")
public interface LostItemReportApi {

    @Operation(summary = "분실물 게시글 신고하기")
    @PostMapping("/articles/lost-item/{id}/reports")
    ResponseEntity<Void> reportLostItemArticle(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid LostItemReportRequest lostItemReportRequest,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer studentId
    );
}
