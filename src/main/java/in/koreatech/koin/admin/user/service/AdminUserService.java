package in.koreatech.koin.admin.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminStudentRepository adminStudentRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminStudentUpdateResponse updateStudent(Integer id, AdminStudentUpdateRequest adminRequest) {
        Student student = adminStudentRepository.getById(id);
        User user = student.getUser();
        checkNicknameDuplication(adminRequest.nickname(), id);
        checkDepartmentValid(adminRequest.major());
        user.update(adminRequest.nickname(), adminRequest.name(),
            adminRequest.phoneNumber(), UserGender.from(adminRequest.gender()));
        user.updateStudentPassword(passwordEncoder, adminRequest.password());
        student.update(adminRequest.studentNumber(), adminRequest.major());
        adminStudentRepository.save(student);

        return AdminStudentUpdateResponse.from(student);
    }

    public void checkNicknameDuplication(String nickname, Integer userId) {
        User checkUser = adminUserRepository.getById(userId);
        if (nickname != null && !nickname.equals(checkUser.getNickname())
            && adminUserRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }

    public void checkDepartmentValid(String department) {
        if (department != null && !StudentDepartment.isValid(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
    }
}
