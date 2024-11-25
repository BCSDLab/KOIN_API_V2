package in.koreatech.koin.admin.user.service;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
import in.koreatech.koin.admin.user.dto.AdminLoginRequest;
import in.koreatech.koin.admin.user.dto.AdminLoginResponse;
import in.koreatech.koin.admin.user.dto.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.user.dto.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminResponse;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.user.dto.AdminUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminsCondition;
import in.koreatech.koin.admin.user.dto.AdminsResponse;
import in.koreatech.koin.admin.user.dto.CreateAdminRequest;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.admin.owner.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.owner.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.admin.student.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminTokenRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final JwtProvider jwtProvider;
    private final AdminStudentRepository adminStudentRepository;
    private final AdminOwnerRepository adminOwnerRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenRepository adminTokenRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        if (!admin.isCanCreateAdmin() || !admin.isSuperAdmin()) {
            throw new AuthorizationException("어드민 계정 생성 권한이 없습니다.");
        }

        validateAdminCreate(request);
        Admin createAdmin = adminRepository.save(request.toEntity(passwordEncoder));

        return AdminResponse.from(createAdmin);
    }

    private void validateAdminCreate(CreateAdminRequest request) {
        EmailAddress emailAddress = EmailAddress.from(request.email());
        emailAddress.validateKoreatechEmail();
        emailAddress.validateAdminEmail();

        validateDuplicateEmail(request);
    }

    private void validateDuplicateEmail(CreateAdminRequest request) {
        adminUserRepository.findByEmail(request.email())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("account: " + request.email());
            });
    }

    @Transactional
    public void adminPasswordChange(AdminPasswordChangeRequest request, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        User user = admin.getUser();
        if (!user.isSamePassword(passwordEncoder, request.oldPassword())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        user.updatePassword(passwordEncoder, request.newPassword());
    }

    @Transactional
    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        User user = adminUserRepository.getByEmail(request.email());
        validateAdminLogin(user, request);

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedtoken = adminTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());

        return AdminLoginResponse.of(accessToken, savedtoken.getRefreshToken());
    }

    private void validateAdminLogin(User user, AdminLoginRequest request) {
        /* 어드민 권한이 없으면 없는 회원으로 간주 */
        if (user.getUserType() != ADMIN) {
            throw UserNotFoundException.withDetail("account" + request.email());
        }

        if (adminRepository.findById(user.getId()).isEmpty()) {
            throw UserNotFoundException.withDetail("account" + request.email());
        }

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        if (!user.isAuthed()) {
            throw new AuthorizationException("PL 인증 대기중입니다.");
        }
    }

    @Transactional
    public void adminLogout(Integer adminId) {
        adminTokenRepository.deleteById(adminId);
    }

    public AdminTokenRefreshResponse adminRefresh(AdminTokenRefreshRequest request) {
        String adminId = getAdminId(request.refreshToken());
        UserToken userToken = adminTokenRepository.getById(Integer.parseInt(adminId));
        if (!Objects.equals(userToken.getRefreshToken(), request.refreshToken())) {
            throw new KoinIllegalArgumentException("refresh token이 일치하지 않습니다.", "request: " + request);
        }
        User user = adminUserRepository.getById(userToken.getId());

        String accessToken = jwtProvider.createToken(user);
        return AdminTokenRefreshResponse.of(accessToken, userToken.getRefreshToken());
    }

    private String getAdminId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return split[split.length - 1];
    }

    public AdminResponse getAdmin(Integer id) {
        Admin admin = adminRepository.getById(id);
        return AdminResponse.from(admin);
    }

    public AdminsResponse getAdmins(AdminsCondition adminsCondition) {
        Integer totalAdmins = adminRepository.countAdmins();
        Criteria criteria = Criteria.of(adminsCondition.page(), adminsCondition.limit(), totalAdmins);

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<Admin> adminsPage = adminRepository.findByConditions(adminsCondition, pageRequest);

        return AdminsResponse.of(adminsPage);
    }

    @Transactional
    public void adminAuthenticate(Integer id, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        if (!admin.isSuperAdmin()) {
            throw new AuthorizationException("어드민 승인 권한이 없습니다.");
        }

        User user = adminRepository.getById(id).getUser();
        user.auth();
    }

    @Transactional
    public void updateAdmin(AdminUpdateRequest request, Integer id) {
        Admin admin = adminRepository.getById(id);
        User user = admin.getUser();

        user.updateName(request.name());
        admin.update(request.teamType(), request.trackType());
    }

    @Transactional
    public void updateAdminPermission(AdminPermissionUpdateRequest request, Integer id, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        if (!admin.isSuperAdmin()) {
            throw new AuthorizationException("슈퍼 어드민 권한이 없습니다.");
        }

        adminRepository.getById(id).updatePermission(request);
    }

    @Transactional
    public User getUser(Integer userId) {
        return adminUserRepository.getById(userId);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User user = adminUserRepository.getById(userId);
        if (user.getUserType() == UserType.STUDENT) {
            adminStudentRepository.deleteById(userId);
        } else if (user.getUserType() == UserType.OWNER) {
            adminOwnerRepository.deleteById(userId);
        }
        adminUserRepository.delete(user);
    }

    @Transactional
    public void undeleteUser(Integer id) {
        User user = adminUserRepository.getById(id);
        user.undelete();
    }
}
