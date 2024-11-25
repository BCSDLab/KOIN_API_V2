package in.koreatech.koin.admin.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.student.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentsResponse;
import in.koreatech.koin.admin.user.dto.StudentsCondition;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.StudentDepartment;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStudentService {

    private final AdminStudentRepository adminStudentRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminStudentsResponse getStudents(StudentsCondition studentsCondition) {
        Integer totalStudents = adminStudentRepository.findAllStudentCount();
        Criteria criteria = Criteria.of(studentsCondition.page(), studentsCondition.limit(), totalStudents);

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<Student> studentsPage = adminStudentRepository.findByConditions(studentsCondition, pageRequest);

        return AdminStudentsResponse.from(studentsPage);
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