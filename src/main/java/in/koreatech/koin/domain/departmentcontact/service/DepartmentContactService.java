package in.koreatech.koin.domain.departmentcontact.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentCategoryContactsResponse;
import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactsResponse;
import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactsResponse.CategoryResponse;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContactDepartment;
import in.koreatech.koin.domain.departmentcontact.repository.DepartmentContactDepartmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentContactService {

    private final DepartmentContactDepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public DepartmentContactsResponse getDepartmentContacts(String keyword) {
        List<DepartmentContactDepartment> departments = departmentRepository.findAllByOrderByDisplayOrderAsc();
        LocalDateTime updatedAt = findLatestUpdatedAt(departments);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<CategoryResponse> categories = Arrays.stream(DepartmentCategory.values())
            .map(category -> CategoryResponse.from(
                category,
                filterDepartments(category, departments, normalizedKeyword)
            ))
            .filter(category -> normalizedKeyword.isEmpty() || !category.departments().isEmpty())
            .toList();

        return new DepartmentContactsResponse(updatedAt, categories);
    }

    @Transactional(readOnly = true)
    public DepartmentCategoryContactsResponse getDepartmentContactsByCategory(
        DepartmentCategory category,
        String keyword
    ) {
        List<DepartmentContactDepartment> departments =
            departmentRepository.findAllByCategoryOrderByDisplayOrderAsc(category);
        LocalDateTime updatedAt = findLatestUpdatedAt(departments);
        List<DepartmentContactDepartment> filteredDepartments = filterDepartments(
            category,
            departments,
            normalizeKeyword(keyword)
        );

        return DepartmentCategoryContactsResponse.from(updatedAt, category, filteredDepartments);
    }

    private List<DepartmentContactDepartment> filterDepartments(
        DepartmentCategory category,
        List<DepartmentContactDepartment> departments,
        String keyword
    ) {
        Stream<DepartmentContactDepartment> categoryDepartments = departments.stream()
            .filter(department -> department.getCategory() == category);

        if (keyword.isEmpty() || contains(category.getDisplayName(), keyword)) {
            return categoryDepartments.toList();
        }
        return categoryDepartments
            .filter(department -> matchesDepartment(department, keyword))
            .toList();
    }

    private boolean matchesDepartment(DepartmentContactDepartment department, String keyword) {
        return contains(department.getName(), keyword) || department.getContacts().stream()
            .anyMatch(contact -> contains(contact.getTask(), keyword));
    }

    private boolean contains(String value, String keyword) {
        return value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime findLatestUpdatedAt(List<DepartmentContactDepartment> departments) {
        return departments.stream()
            .flatMap(department -> Stream.concat(
                Stream.of(department.getUpdatedAt()),
                department.getContacts().stream().map(contact -> contact.getUpdatedAt())
            ))
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
}
