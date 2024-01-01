package in.koreatech.koin.domain.dept.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dept_nums")
@IdClass(DeptNumId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeptNum {

    @Id
    @NotNull
    @Column(name = "dept_num", nullable = false)
    private Long number;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_name")
    private DeptInfo deptInfo;

    @Builder
    private DeptNum(Long number, DeptInfo deptInfo) {
        this.number = number;
        this.deptInfo = deptInfo;
    }
}
