package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.exception.AbtestNotIncludeVariableException;
import in.koreatech.koin.admin.abtest.exception.AbtestVariableIllegalArgumentException;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(name = "display_title", nullable = false)
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
    @Enumerated(EnumType.STRING)
    private AbtestStatus status;

    @OneToMany(mappedBy = "abtest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbtestVariable> abtestVariables = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private AbtestVariable winner;

    @Builder
    private Abtest(
        Integer id,
        String title,
        String displayTitle,
        String description,
        String creator,
        String team,
        AbtestStatus status
    ) {
        this.id = id;
        this.title = title;
        this.displayTitle = displayTitle;
        this.description = description;
        this.creator = creator;
        this.team = team;
        this.status = status;
    }

    public AbtestVariable assignVariable(List<AbtestVariableCount> cacheCounts) {
        Map<Integer, Integer> dbCount = abtestVariables.stream()
            .collect(Collectors.toMap(AbtestVariable::getId, AbtestVariable::getCount));
        Map<Integer, Integer> cacheCount = cacheCounts.stream()
            .collect(Collectors.toMap(AbtestVariableCount::getVariableId, AbtestVariableCount::getCount));
        Map<Integer, Integer> count = merge(dbCount, cacheCount, 1);
        int totalCount = count.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, Integer> nowRate = count.entrySet().stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue() / totalCount * 100))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Integer, Integer> dbRate = abtestVariables.stream()
            .collect(Collectors.toMap(AbtestVariable::getId, AbtestVariable::getRate));
        Map<Integer, Integer> differenceRate = merge(dbRate, nowRate, -1);
        int targetVariable = differenceRate.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> AbtestNotIncludeVariableException.withDetail("abtest name: " + title))
            .getKey();

        return abtestVariables.stream()
            .filter(abtestVariable -> abtestVariable.getId() == targetVariable)
            .findAny()
            .get();
    }

    /**
     * 두 Map에서 동일한 key를 가지는 value를 합치는 함수. 단, 1번째 매개변수로 들어온 Map에 merge하기 때문에 side effect 존재.
     * @param m1 병합할 Map 1 (병합 결과가 기록되는 곳)
     * @param m2 병합할 Map 2
     * @param weight 가중치. 병합 시 m2에 weight만큼 곱하여 더함
     * @return 병합된 Map(m1) 반환
     */
    private Map<Integer, Integer> merge(Map<Integer, Integer> m1, Map<Integer, Integer> m2, int weight) {
        m2.forEach((key, value) -> m1.merge(key, weight * value, Integer::sum));
        return m1;
    }

    public void setVariables(List<AbtestRequest.InnerVariableRequest> variables, EntityManager entityManager) {
        vaildateVariables(variables);
        List<AbtestVariable> saved = variables.stream()
            .map(request ->
                AbtestVariable.builder()
                    .abtest(this)
                    .displayName(request.displayName())
                    .rate(request.rate())
                    .name(request.name())
                    .build()
            ).toList();
        abtestVariables.clear();
        entityManager.flush();
        abtestVariables.addAll(saved);
    }

    private static void vaildateVariables(List<AbtestRequest.InnerVariableRequest> variables) {
        int sum = variables.stream().mapToInt(AbtestRequest.InnerVariableRequest::rate).sum();
        if (sum != 100) {
            throw AbtestVariableIllegalArgumentException.withDetail("실험군 비율 합이 100이 아닙니다. rate sum: " + sum);
        }

        int distinctSize = variables.stream()
            .map(variable -> variable.name())
            .distinct().toList().size();
        if (distinctSize != variables.size()) {
            throw AbtestVariableIllegalArgumentException.withDetail("실험군 간의 변수명이 중복됩니다.");
        }
    }

    public String getWinnerName() {
        return winner != null ? winner.getName() : null;
    }
}
