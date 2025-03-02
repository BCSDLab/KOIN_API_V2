package in.koreatech.koin.domain.student.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "major")
@NoArgsConstructor(access = PROTECTED)
public class Major extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 255)
    @Column(name = "name", length = 255)
    private String name;

    @JoinColumn(name = "department_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    @Builder
    public Major(String name, Department department) {
        this.name = name;
        this.department = department;
    }
}
