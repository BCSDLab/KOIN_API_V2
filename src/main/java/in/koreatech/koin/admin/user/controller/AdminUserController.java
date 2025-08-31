package in.koreatech.koin.admin.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.user.service.AdminUserService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi{

    private final AdminUserService adminUserService;

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<User> getUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getUser(id));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/users/{id}/undelete")
    public ResponseEntity<Void> undeleteUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.undeleteUser(id);
        return ResponseEntity.ok().build();
    }
}
