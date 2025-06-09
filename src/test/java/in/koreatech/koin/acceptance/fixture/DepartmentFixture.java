package in.koreatech.koin.acceptance.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class DepartmentFixture {

    private final DepartmentRepository departmentRepository;

    public DepartmentFixture(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public void 전체학부() {
        List<Department> departments = List.of(
            Department.builder().name("건축공학부").build(),
            Department.builder().name("고용서비스정책학과").build(),
            Department.builder().name("기계공학부").build(),
            Department.builder().name("디자인공학부").build(),
            Department.builder().name("메카트로닉스공학부").build(),
            Department.builder().name("산업경영학부").build(),
            Department.builder().name("전기전자통신공학부").build(),
            Department.builder().name("컴퓨터공학부").build(),
            Department.builder().name("화학생명공학부").build(),
            Department.builder().name("에너지신소재공학부").build(),
            Department.builder().name("에너지신소재화학공학부").build(),
            Department.builder().name("안전공학부").build()
        );

        departmentRepository.saveAll(departments);
    }

    public Department 컴퓨터공학부() {
        return departmentRepository.save(
            Department.builder()
                .name("컴퓨터공학부")
                .build()
        );
    }

    public Department 기계공학부() {
        return departmentRepository.save(
            Department.builder()
                .name("기계공학부")
                .build()
        );
    }
}
