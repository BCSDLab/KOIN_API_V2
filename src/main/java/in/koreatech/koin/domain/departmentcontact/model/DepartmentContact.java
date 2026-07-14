package in.koreatech.koin.domain.departmentcontact.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "task", nullable = false, length = 255)
    private String task;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    public DepartmentContact(String department, String task, String phoneNumber) {
        this.department = department;
        this.task = task;
        this.phoneNumber = phoneNumber;
    }
}
