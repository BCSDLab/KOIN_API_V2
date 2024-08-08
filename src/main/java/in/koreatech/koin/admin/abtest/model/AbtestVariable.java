package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "abtest_variable", schema = "koin")
public class AbtestVariable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abtest_id", nullable = false)
    private Abtest abtest;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "rate")
    private Integer rate;

    @Column(name = "count")
    private Integer count;

    @NotNull
    @Column(name = "is_before", nullable = false)
    private Boolean isBefore = false;

    @OneToMany(mappedBy = "variable", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccessHistoryAbtestVariable> accessHistoryAbtestVariables = new ArrayList<>();

    @Builder
    private AbtestVariable(
        Integer id,
        Abtest abtest,
        String name,
        String displayName,
        Integer rate,
        Integer count,
        Boolean isBefore
    ) {
        this.id = id;
        this.abtest = abtest;
        this.name = name;
        this.displayName = displayName;
        this.rate = rate;
        this.count = count;
        this.isBefore = isBefore;
    }

    public void addCount(int count) {
        this.count += count;
    }
}
