package in.koreatech.koin.admin.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.NewOwnersCondition;
import in.koreatech.koin.admin.user.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.user.repository.AdminShopRepository;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
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

    public AdminNewOwnersResponse getNewOwners(NewOwnersCondition newOwnersCondition) {
        newOwnersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminOwnerRepository.findUnauthenticatedOwnersCount();
        Criteria criteria = Criteria.of(newOwnersCondition.page(), newOwnersCondition.limit(), totalOwners);
        Sort.Direction direction = newOwnersCondition.getDirection();

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));
        Page<Owner> result;

        if (newOwnersCondition.searchType() == NewOwnersCondition.SearchType.EMAIL) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByEmail(newOwnersCondition.query(), pageRequest);
        } else if (newOwnersCondition.searchType() == NewOwnersCondition.SearchType.NAME) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByName(newOwnersCondition.query(), pageRequest);
        } else {
            result = adminOwnerRepository.findPageUnauthenticatedOwners(pageRequest);
        }

        List<OwnerIncludingShop> ownerIncludingShop = new ArrayList<>();
        for (Owner owner : result.getContent()) {
            List<Shop> shops = adminShopRepository.findAllByOwnerId(owner.getId());
            if (shops.isEmpty()) {
                ownerIncludingShop.add(OwnerIncludingShop.of(owner));
            } else {
                shops.forEach(shop -> ownerIncludingShop.add(OwnerIncludingShop.of(owner, shop)));
            }
        }

        return AdminNewOwnersResponse.of(result, criteria, ownerIncludingShop);
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
}
