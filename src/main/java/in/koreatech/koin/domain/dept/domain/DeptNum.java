package in.koreatech.koin.domain.dept.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dept_nums")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeptNum {

    /*@MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dept_name", nullable = false)
    private Dept deptName;*/

    //@MapsId
    // @NotNull
    // @Size(max = 45)
    // @Column(name = "dept_name", nullable = false, length = 45)
    // private String name;

    @Id
    @NotNull
    @Column(name = "dept_num", nullable = false)
    private Long number;

    // @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_name")
    private Dept dept;

    @Builder
    private DeptNum(String name, Long number) {

        this.number = number;
    }
}