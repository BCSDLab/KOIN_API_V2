package in.koreatech.koin.admin.user.service;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.admin.user.dto.AdminLoginRequest;
import in.koreatech.koin.admin.user.dto.AdminLoginResponse;
import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.user.dto.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentsResponse;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.user.dto.AdminUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminsCondition;
import in.koreatech.koin.admin.user.dto.AdminsResponse;
import in.koreatech.koin.admin.user.dto.CreateAdminRequest;
import in.koreatech.koin.admin.user.dto.OwnersCondition;
import in.koreatech.koin.admin.user.dto.StudentsCondition;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.admin.user.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.user.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminTokenRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.StudentDepartment;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
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
    private final AdminOwnerShopRedisRepository adminOwnerShopRedisRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminShopRepository adminShopRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenRepository adminTokenRepository;
    private final AdminRepository adminRepository;

    public AdminStudentsResponse getStudents(StudentsCondition studentsCondition) {
        Integer totalStudents = adminStudentRepository.findAllStudentCount();
        Criteria criteria = Criteria.of(studentsCondition.page(), studentsCondition.limit(), totalStudents);

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<Student> studentsPage = adminStudentRepository.findByConditions(studentsCondition, pageRequest);

        return AdminStudentsResponse.from(studentsPage);
    }

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
                throw DuplicationEmailException.withDetail("email: " + request.email());
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
            throw UserNotFoundException.withDetail("email" + request.email());
        }

        if (adminRepository.findById(user.getId()).isEmpty()) {
            throw UserNotFoundException.withDetail("email" + request.email());
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
    public void allowOwnerPermission(Integer id) {
        Owner owner = adminOwnerRepository.getById(id);
        owner.getUser().auth();
        Optional<OwnerShop> ownerShop = adminOwnerShopRedisRepository.findById(id);
        if (ownerShop.isPresent()) {
            Integer shopId = ownerShop.get().getShopId();
            if (shopId != null) {
                Shop shop = adminShopRepository.getById(shopId);
                shop.updateOwner(owner);
                owner.setGrantShop(true);
            }
            adminOwnerShopRedisRepository.deleteById(id);
        }
    }

    public AdminStudentResponse getStudent(Integer userId) {
        Student student = adminStudentRepository.getById(userId);
        return AdminStudentResponse.from(student);
    }

    @Transactional
    public AdminStudentUpdateResponse updateStudent(Integer id, AdminStudentUpdateRequest adminRequest) {
        Student student = adminStudentRepository.getById(id);
        User user = student.getUser();
        validateNicknameDuplication(adminRequest.nickname(), id);
        validateDepartmentValid(adminRequest.major());
        user.update(adminRequest.nickname(), adminRequest.name(),
            adminRequest.phoneNumber(), UserGender.from(adminRequest.gender()));
        user.updateStudentPassword(passwordEncoder, adminRequest.password());
        student.update(adminRequest.studentNumber(), adminRequest.major());
        adminStudentRepository.save(student);

        return AdminStudentUpdateResponse.from(student);
    }

    public AdminNewOwnersResponse getNewOwners(OwnersCondition ownersCondition) {
        ownersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminUserRepository.findUsersCountByUserTypeAndIsAuthed(UserType.OWNER, false);
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<Owner> result = getNewOwnersResultPage(ownersCondition, criteria, direction);

        List<OwnerIncludingShop> ownerIncludingShops = result.getContent().stream()
            .map(owner -> {
                Optional<OwnerShop> ownerShop = adminOwnerShopRedisRepository.findById(owner.getId());
                return ownerShop
                    .map(os -> {
                        Shop shop = adminShopRepository.findById(os.getShopId()).orElse(null);
                        return OwnerIncludingShop.of(owner, shop);
                    })
                    .orElseGet(() -> OwnerIncludingShop.of(owner, null));
            })
            .collect(Collectors.toList());

        return AdminNewOwnersResponse.of(ownerIncludingShops, result, criteria);
    }

    public AdminOwnersResponse getOwners(OwnersCondition ownersCondition) {
        ownersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminUserRepository.findUsersCountByUserTypeAndIsAuthed(UserType.OWNER, true);
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<Owner> result = getOwnersResultPage(ownersCondition, criteria, direction);

        return AdminOwnersResponse.of(result, criteria);
    }

    private Page<Owner> getOwnersResultPage(OwnersCondition ownersCondition, Criteria criteria,
        Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));

        Page<Owner> result;

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            result = adminOwnerRepository.findPageOwnersByEmail(ownersCondition.query(), pageRequest);
        } else if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            result = adminOwnerRepository.findPageOwnersByName(ownersCondition.query(), pageRequest);
        } else {
            result = adminOwnerRepository.findPageOwners(pageRequest);
        }

        return result;
    }

    private Page<Owner> getNewOwnersResultPage(OwnersCondition ownersCondition, Criteria criteria,
        Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));

        Page<Owner> result;

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByEmail(ownersCondition.query(), pageRequest);
        } else if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByName(ownersCondition.query(), pageRequest);
        } else {
            result = adminOwnerRepository.findPageUnauthenticatedOwners(pageRequest);
        }

        return result;
    }

    private void validateNicknameDuplication(String nickname, Integer userId) {
        if (nickname != null &&
            adminUserRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }

    private void validateDepartmentValid(String department) {
        if (department != null && !StudentDepartment.isValid(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
    }

    public AdminOwnerResponse getOwner(Integer ownerId) {
        Owner owner = adminOwnerRepository.getById(ownerId);

        List<Integer> shopsId = adminShopRepository.findAllByOwnerId(ownerId)
            .stream()
            .map(Shop::getId)
            .toList();

        return AdminOwnerResponse.of(owner, shopsId);
    }

    @Transactional
    public AdminOwnerUpdateResponse updateOwner(Integer ownerId, AdminOwnerUpdateRequest request) {
        Owner owner = adminOwnerRepository.getById(ownerId);
        owner.update(request);
        return AdminOwnerUpdateResponse.from(owner);
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
