package in.koreatech.koin.fixture;

import in.koreatech.koin.domain.graduation.model.Department;
import in.koreatech.koin.domain.graduation.repository.DepartmentRepository;
import org.springframework.stereotype.Component;

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
