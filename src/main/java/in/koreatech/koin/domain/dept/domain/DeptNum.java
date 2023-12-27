package in.koreatech.koin.domain.dept.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dept_nums")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeptNum {

    @Id
    @NotNull
    @Size(max = 45)
    @Enumerated(EnumType.STRING)
    @Column(name = "dept_name", nullable = false, length = 45)
    private DeptType name;

    @NotNull
    @Column(name = "dept_num", nullable = false)
    private Long number;

    @Builder
    private DeptNum(DeptType name, Long number) {
        this.name = name;
        this.number = number;
    }
}