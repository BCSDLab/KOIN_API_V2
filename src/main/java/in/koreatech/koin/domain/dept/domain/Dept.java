package in.koreatech.koin.domain.dept.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "name")
    private DeptNum deptNum;

    @Builder
    private Dept(String name, String curriculumLink) {
        this.name = name;
        this.curriculumLink = curriculumLink;
    }
}
