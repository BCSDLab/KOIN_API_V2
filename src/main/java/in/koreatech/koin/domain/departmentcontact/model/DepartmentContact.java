package in.koreatech.koin.domain.departmentcontact.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "department_contacts")
@NoArgsConstructor(access = PROTECTED)
public class DepartmentContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentContactDepartment department;

    @Column(name = "task", nullable = false, length = 255)
    private String task;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public DepartmentContact(
        DepartmentContactDepartment department,
        String task,
        String phoneNumber,
        Integer displayOrder
    ) {
        this.department = department;
        this.task = task;
        this.phoneNumber = phoneNumber;
        this.displayOrder = displayOrder;
        department.addContact(this);
    }
}
