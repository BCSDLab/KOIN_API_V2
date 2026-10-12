package in.koreatech.koin.domain.student.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "department")
@NoArgsConstructor(access = PROTECTED)
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "curriculum_link", nullable = false)
    private String curriculumLink = "https://www.koreatech.ac.kr/menu.es?mid=a10201000000";

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dept_nums", nullable = false, columnDefinition = "json")
    private List<String> deptNums = List.of("N/A");

    @Column(name = "is_selectable", nullable = false)
    private boolean isSelectable = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 1000;

    @Builder
    public Department(String name) {
        this.name = name;
    }
}
