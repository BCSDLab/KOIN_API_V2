package in.koreatech.koin.domain.club.like.controller;

import in.koreatech.koin.domain.club.like.service.ClubLikeService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubId}/like")
public class ClubLikeController implements ClubLikeApi {

    private final ClubLikeService clubLikeService;

    @PutMapping
    public ResponseEntity<Void> likeClub(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        clubLikeService.likeClub(clubId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Void> likeClubCancel(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        clubLikeService.likeClubCancel(clubId, userId);
        return ResponseEntity.ok().build();
    }
}
