package in.koreatech.koin.domain.dept.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dept_infos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dept {

    @Id
    @NotNull
    @Size(max = 45)
    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @NotNull
    @Size(max = 255)
    @Column(name = "curriculum_link", nullable = false, length = 255)
    private String curriculumLink;

    @NotNull
    @Column(name = "is_deleted", nullable = true)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "dept")
    private List<DeptNum> deptNums = new ArrayList<>();


    @Builder
    private Dept(String name, String curriculumLink) {
        this.name = name;
        this.curriculumLink = curriculumLink;
    }
}
