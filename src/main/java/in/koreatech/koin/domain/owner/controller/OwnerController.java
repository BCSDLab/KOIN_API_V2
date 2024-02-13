package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.service.OwnerService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OwnerController implements OwnerApi {

    private final OwnerService ownerService;

    @Override
    public ResponseEntity<OwnerResponse> getOwner(
        @Auth(permit = {OWNER}) Long ownerId
    ) {
        OwnerResponse ownerInfo = ownerService.getOwner(ownerId);
        return ResponseEntity.ok().body(ownerInfo);
    }
}
