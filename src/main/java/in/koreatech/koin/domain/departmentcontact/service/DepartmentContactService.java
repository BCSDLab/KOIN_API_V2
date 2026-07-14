package in.koreatech.koin.domain.departmentcontact.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactResponse;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;
import in.koreatech.koin.domain.departmentcontact.repository.DepartmentContactRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentContactService {

    private final DepartmentContactRepository departmentContactRepository;

    @Transactional(readOnly = true)
    public List<DepartmentContactResponse> getDepartmentContacts(String department) {
        String normalizedDepartment = normalizeDepartment(department);
        List<DepartmentContact> contacts = normalizedDepartment == null
            ? departmentContactRepository.findAllByOrderByIdAsc()
            : departmentContactRepository.findAllByDepartmentOrderByIdAsc(normalizedDepartment);

        return contacts.stream()
            .map(DepartmentContactResponse::from)
            .toList();
    }

    private String normalizeDepartment(String department) {
        if (department == null || department.isBlank()) {
            return null;
        }
        return department.trim();
    }
}
