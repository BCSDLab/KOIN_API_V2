package in.koreatech.koin.domain.club.qna.controller;

import in.koreatech.koin.domain.club.qna.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.qna.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.qna.service.ClubQnaService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubId}/qna")
public class ClubQnaController implements ClubQnaApi {

    private final ClubQnaService clubQnaService;

    @GetMapping("/{clubId}/qna")
    public ResponseEntity<ClubQnasResponse> getQnas(
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        ClubQnasResponse response = clubQnaService.getQnas(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{clubId}/qna")
    public ResponseEntity<Void> createQna(
        @RequestBody @Valid ClubQnaCreateRequest request,
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubQnaService.createQna(request, clubId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{clubId}/qna/{qnaId}")
    public ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubQnaService.deleteQna(clubId, qnaId, studentId);
        return ResponseEntity.noContent().build();
    }
}
