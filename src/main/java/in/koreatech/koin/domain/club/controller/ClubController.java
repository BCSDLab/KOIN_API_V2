package in.koreatech.koin.domain.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.club.dto.request.CreateClubRequest;
import in.koreatech.koin.domain.club.dto.request.CreateQnaRequest;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.GetClubByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.QnasResponse;
import in.koreatech.koin.domain.club.service.ClubService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController implements ClubApi {

    private final ClubService clubService;

    @PostMapping
    public ResponseEntity<Void> createClub(
        @RequestBody @Valid CreateClubRequest createClubRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    ){
        clubService.createClub(createClubRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<GetClubByCategoryResponse> getClubByCategory(
        @Parameter(in = PATH) @PathVariable Integer categoryId,
        @RequestParam(required = false) String sort
    ){
        GetClubByCategoryResponse response = clubService.getClubByCategory(categoryId, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponse> getClub(
        @Parameter(in = PATH) @PathVariable Integer clubId
    ){
        ClubResponse response = clubService.getClub(clubId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    public ResponseEntity<ClubHotResponse> getHotClub() {
        ClubHotResponse response = clubService.getHotClub();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/qna")
    public ResponseEntity<QnasResponse> getQnas(
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        QnasResponse response = clubService.getQnas(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{clubId}/qna")
    public ResponseEntity<Void> createQna(
        @RequestBody @Valid CreateQnaRequest request,
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ){
        clubService.createQna(request, clubId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{clubId}/qna/{qnaId}")
    public ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.deleteQna(clubId, qnaId, studentId);
        return ResponseEntity.noContent().build();
    }
}
