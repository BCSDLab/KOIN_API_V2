package in.koreatech.koin.admin.operators.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.admin.operators.dto.request.AdminLoginRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminUpdateRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminsCondition;
import in.koreatech.koin.admin.operators.dto.response.AdminLoginResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminsResponse;
import in.koreatech.koin.admin.operators.dto.response.CreateAdminRequest;
import in.koreatech.koin.admin.operators.model.Admin;
import in.koreatech.koin.admin.operators.repository.AdminRepository;
import in.koreatech.koin.admin.user.validation.AdminUserValidation;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final JwtProvider jwtProvider;
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
        return AdminTokenRefreshResponse.of(accessToken,
            refreshTokenService.getRefreshToken(adminId, userAgentInfo.getType()));
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
}
