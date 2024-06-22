package in.koreatech.koin.admin.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.OwnersCondition;
import in.koreatech.koin.admin.user.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.user.repository.AdminShopRepository;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminStudentRepository adminStudentRepository;
    private final AdminOwnerRepository adminOwnerRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminShopRepository adminShopRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void allowOwnerPermission(Integer id) {
        Owner owner = adminOwnerRepository.getById(id);
        owner.getUser().auth();
        owner.setGrantShop(true);
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

        Integer totalOwners = adminOwnerRepository.findUnauthenticatedOwnersCount();
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<OwnerIncludingShop> result = getResultPage(ownersCondition, criteria, direction);

        return AdminNewOwnersResponse.of(result, criteria);
    }

    public AdminOwnersResponse getOwners(OwnersCondition ownersCondition) {
        ownersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminOwnerRepository.countByUserUserType(UserType.OWNER);
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<OwnerIncludingShop> result = getResultPage(ownersCondition, criteria, direction);

        return AdminOwnersResponse.of(result, criteria);
    }

    private Page<OwnerIncludingShop> getResultPage(OwnersCondition ownersCondition, Criteria criteria, Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));

        Page<OwnerIncludingShop> result;

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
            .collect(Collectors.toList());

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
}
