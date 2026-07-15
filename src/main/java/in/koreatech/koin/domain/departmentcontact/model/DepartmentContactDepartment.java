package in.koreatech.koin.domain.departmentcontact.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "department_contact_departments")
@NoArgsConstructor(access = PROTECTED)
public class DepartmentContactDepartment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private DepartmentCategory category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @OrderBy("displayOrder ASC")
    @OneToMany(mappedBy = "department")
    private List<DepartmentContact> contacts = new ArrayList<>();

    public DepartmentContactDepartment(DepartmentCategory category, String name, Integer displayOrder) {
        this.category = category;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    void addContact(DepartmentContact contact) {
        contacts.add(contact);
    }
}
