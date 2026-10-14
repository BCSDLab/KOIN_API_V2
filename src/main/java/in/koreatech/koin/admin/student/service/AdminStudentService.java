package in.koreatech.koin.admin.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.admin.student.dto.AdminStudentResponse;
import in.koreatech.koin.admin.student.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.student.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.student.dto.AdminStudentsResponse;
import in.koreatech.koin.admin.student.dto.StudentsCondition;
import in.koreatech.koin.admin.student.repository.AdminDepartmentRepository;
import in.koreatech.koin.admin.student.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.service.StudentValidationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStudentService {

    private final AdminStudentRepository adminStudentRepository;
    private final AdminDepartmentRepository adminDepartmentRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentValidationService studentValidationService;

    public AdminStudentResponse getStudent(Integer userId) {
        Student student = adminStudentRepository.getById(userId);
        return AdminStudentResponse.from(student);
    }

    public AdminStudentsResponse getStudents(StudentsCondition studentsCondition) {
        Integer totalStudents = adminStudentRepository.findAllStudentCount();
        Criteria criteria = Criteria.of(studentsCondition.page(), studentsCondition.limit(), totalStudents);

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<Student> studentsPage = adminStudentRepository.findByConditions(studentsCondition, pageRequest);

        return AdminStudentsResponse.from(studentsPage);
    }

    @Transactional
    public AdminStudentUpdateResponse updateStudent(Integer id, AdminStudentUpdateRequest adminRequest) {
        Student student = adminStudentRepository.getById(id);
        User user = student.getUser();
        validateNicknameDuplication(adminRequest.nickname(), id);
        studentValidationService.validateDepartment(adminRequest.major());
        Department department = adminDepartmentRepository.getByName(adminRequest.major());
        studentValidationService.validateAcademicProgram(adminRequest.studentNumber(), department, student.getMajor());
        user.update(user.getEmail(), adminRequest.nickname(), adminRequest.name(),
            adminRequest.phoneNumber(), UserGender.from(adminRequest.gender()));
        user.updatePassword(passwordEncoder, adminRequest.password());
        student.updateInfo(adminRequest.studentNumber(), department);
        adminStudentRepository.save(student);

        return AdminStudentUpdateResponse.from(student);
    }

    private void validateNicknameDuplication(String nickname, Integer userId) {
        if (nickname != null &&
            adminUserRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw CustomException.of(ApiResponseCode.DUPLICATE_NICKNAME, "nickname : " + nickname);
        }
    }
}
