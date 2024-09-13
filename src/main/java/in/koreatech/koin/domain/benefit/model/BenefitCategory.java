package in.koreatech.koin.domain.benefit.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop_benefit_categories")
@NoArgsConstructor(access = PROTECTED)
public class BenefitCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(min = 5, max = 20)
    @Column(name = "title", nullable = false)
    String title;

    @Size(min = 10, max = 100)
    @Column(name = "detail")
    String detail;
}
