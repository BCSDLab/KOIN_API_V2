package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin._common.model.BaseEntity;
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

    @Column(name = "rate", nullable = false)
    private Integer rate;

    @Column(name = "count", nullable = false)
    private Integer count = 0;

    @OneToMany(mappedBy = "variable", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccessHistoryAbtestVariable> accessHistoryAbtestVariables = new ArrayList<>();

    @Builder
    private AbtestVariable(
        Integer id,
        Abtest abtest,
        String name,
        String displayName,
        Integer rate,
        Integer count
    ) {
        this.id = id;
        this.abtest = abtest;
        this.name = name;
        this.displayName = displayName;
        this.rate = rate;
        this.count = count != null ? count : 0;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public void update(String displayName, Integer rate) {
        this.displayName = displayName;
        this.rate = rate;
    }

    public void close() {
        accessHistoryAbtestVariables.clear();
    }
}
