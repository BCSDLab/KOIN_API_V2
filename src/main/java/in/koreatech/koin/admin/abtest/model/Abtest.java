package in.koreatech.koin.admin.abtest.model;

import static java.util.Arrays.stream;
import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.ColumnDefault;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "abtest", schema = "koin")
public class Abtest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 255)
    @NotNull
    @Column(name = "display_name", nullable = false)
    private String displayTitle;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @Column(name = "creator")
    private String creator;

    @Size(max = 50)
    @Column(name = "team")
    private String team;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "abtest", cascade = CascadeType.PERSIST)
    private List<AbtestVariable> abtestVariables = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winnder_id")
    private AbtestVariable winner;

    @Builder
    private Abtest(
        Integer id,
        String title,
        String displayTitle,
        String description,
        String creator,
        String team,
        String status
    ) {
        this.id = id;
        this.title = title;
        this.displayTitle = displayTitle;
        this.description = description;
        this.creator = creator;
        this.team = team;
        this.status = status;
    }

    public String assignVariable(List<AbtestCount> cacheCounts) {
        Map<Integer, Integer> cacheCount = cacheCounts.stream()
            .collect(Collectors.toMap(
                AbtestCount::getVariableId,
                AbtestCount::getCount
            ));

        Map<Integer, Integer> dbCount = abtestVariables.stream()
            .collect(Collectors.toMap(
                AbtestVariable::getId,
                AbtestVariable::getCount
            ));

        dbCount.forEach((key, value) -> cacheCount.merge(key, value, (v1, v2) -> v1 + v2));

        int cacheCount = cacheCounts.stream()
            .mapToInt(AbtestCount::getCount)
            .sum();

        int dbCount = abtestVariables.stream()
            .mapToInt(AbtestVariable::getCount)
            .sum();

        int totalCount = cacheCount + dbCount;




        List<Double> nowRate = abtestVariables.stream()
            .mapToInt(AbtestVariable::getCount)
            .mapToDouble(count -> count / totalCount * 100)
            .boxed()
            .toList();

    }
}
