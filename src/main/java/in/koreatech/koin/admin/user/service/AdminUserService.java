package in.koreatech.koin.admin.user.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.admin.owner.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.student.repository.AdminStudentRepository;
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
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.admin.user.validation.AdminUserValidation;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
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
    private final AdminRepository adminRepository;
    private final AdminUserValidation adminUserValidation;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        if (!admin.isCanCreateAdmin() || !admin.isSuperAdmin()) {
            throw new AuthorizationException("어드민 계정 생성 권한이 없습니다.");
        }

        adminUserValidation.validateEmailForAdminCreated(request.email());
        Admin savedAdmin = adminRepository.save(request.toAdmin(passwordEncoder));

        return AdminResponse.from(savedAdmin);
    }

    @Transactional
    public void adminPasswordChange(AdminPasswordChangeRequest request, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        User user = admin.getUser();
        user.requireSameLoginPw(passwordEncoder, request.oldPassword());
        user.updatePassword(passwordEncoder, request.newPassword());
    }

    @Transactional
    public AdminLoginResponse adminLogin(AdminLoginRequest request, UserAgentInfo userAgentInfo) {
        Admin admin = adminRepository.getByEmail(request.email());
        User user = admin.getUser();
        adminUserValidation.validateAdminLogin(user, request);

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgentInfo.getType());
        user.updateLastLoggedTime(LocalDateTime.now());

        return AdminLoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public void adminLogout(Integer adminId, UserAgentInfo userAgentInfo) {
        refreshTokenService.deleteRefreshToken(adminId, userAgentInfo.getType());
    }

    public AdminTokenRefreshResponse adminRefresh(AdminTokenRefreshRequest request, UserAgentInfo userAgentInfo) {
        Integer adminId = refreshTokenService.extractUserId(request.refreshToken());
        refreshTokenService.verifyRefreshToken(adminId, userAgentInfo.getType(), request.refreshToken());
        User user = adminUserRepository.getById(adminId);
        String accessToken = jwtProvider.createToken(user);
        return AdminTokenRefreshResponse.of(accessToken, refreshTokenService.getRefreshToken(adminId, userAgentInfo.getType()));
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
        user.permitAuth();
    }

    @Transactional
    public void updateAdmin(AdminUpdateRequest request, Integer id) {
        Admin admin = adminRepository.getById(id);
        User user = admin.getUser();

        user.updateName(request.name());
        admin.updateTeamTrack(request.teamType(), request.trackType());
    }

    @Transactional
    public void updateAdminPermission(AdminPermissionUpdateRequest request, Integer id, Integer adminId) {
        Admin admin = adminRepository.getById(adminId);
        if (!admin.isSuperAdmin()) {
            throw new AuthorizationException("슈퍼 어드민 권한이 없습니다.");
        }

        adminRepository.getById(id).updatePermission(request.canCreateAdmin(), request.superAdmin());
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
