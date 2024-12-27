package in.koreatech.koin.fixture;

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

    public Department 컴퓨터공학부() {
        return departmentRepository.save(
            Department.builder()
                .name("컴퓨터공학부")
                .build()
        );
    }
}
