package in.koreatech.koin.domain.user.controller;

import in.koreatech.koin.domain.auth.UserAuth;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(@UserAuth User user) {
        userService.logout(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/refresh")
    public ResponseEntity<UserTokenRefreshResponse> refresh(
        @UserAuth User user,
        @RequestBody @Valid UserTokenRefreshRequest request
    ) {
        UserTokenRefreshResponse tokenGroupResponse = userService.refresh(user, request);
        return ResponseEntity.ok().body(tokenGroupResponse);
    }
}
