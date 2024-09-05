package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import in.koreatech.koin.admin.abtest.exception.AbtestAssignException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotIncludeVariableException;
import in.koreatech.koin.admin.abtest.exception.AbtestTitleIllegalArgumentException;
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

    public AbtestVariable findAssignVariable(List<AbtestVariableCount> cacheCounts) {
        // dbCount와 cacheCount를 병합하여 현재 카운트를 계산
        Map<Integer, Integer> currentCounts = abtestVariables.stream()
            .collect(Collectors.toMap(AbtestVariable::getId, AbtestVariable::getCount));

        cacheCounts.forEach(count -> currentCounts.merge(
            count.getVariableId(),
            count.getCount(),
            Integer::sum
        ));

        // 총 레코드 수 계산
        int totalCount = currentCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalCount == 0) {
            return abtestVariables.get(0);
        }

        // 각 변수의 차이 계산하여 가장 큰 차이를 갖는 변수 선택
        int targetVariable = abtestVariables.stream()
            .map(variable -> {
                int currentRate = currentCounts.get(variable.getId()) * 100 / totalCount;
                int difference = variable.getRate() - currentRate;
                return new AbstractMap.SimpleEntry<>(variable.getId(), difference);
            })
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> AbtestAssignException.withDetail("abtest name: " + title))
            .getKey();

        // 타겟 변수 반환
        return abtestVariables.stream()
            .filter(variable -> variable.getId() == targetVariable)
            .findAny()
            .orElseThrow(() -> AbtestAssignException.withDetail("abtest name: " + title));
    }

    public void setVariables(List<AbtestVariable> variables, EntityManager entityManager) {
        validateVariables(variables);
        abtestVariables.clear();
        entityManager.flush();
        abtestVariables.addAll(
            variables.stream().map(request ->
                AbtestVariable.builder()
                    .abtest(this)
                    .displayName(request.getDisplayName())
                    .rate(request.getRate())
                    .name(request.getName())
                    .build()
            ).toList());
    }

    public void update(Abtest requestedAbtest, EntityManager entityManager) {
        if (!title.equals(requestedAbtest.title)) {
            throw AbtestTitleIllegalArgumentException.withDetail("실험 title은 변경할 수 없습니다.");
        }
        validateVariables(requestedAbtest.abtestVariables);
        updateVariables(requestedAbtest.abtestVariables, entityManager);
        displayTitle = requestedAbtest.displayTitle;
        creator = requestedAbtest.creator;
        team = requestedAbtest.team;
        description = requestedAbtest.description;
    }

    private void updateVariables(List<AbtestVariable> requestVariables,
        EntityManager entityManager) {
        abtestVariables.removeIf(abtestVariable ->
            requestVariables.stream().noneMatch(requestVariable ->
                requestVariable.getName().equals(abtestVariable.getName())
            ));

        requestVariables.forEach(requestVariable -> {
            Optional<AbtestVariable> variable = abtestVariables.stream()
                .filter(abtestVariable -> abtestVariable.getName().equals(requestVariable.getName()))
                .findAny();
            if (variable.isPresent()) {
                variable.get().update(requestVariable.getDisplayName(), requestVariable.getRate());
            }
            if (variable.isEmpty()) {
                AbtestVariable newVariable = AbtestVariable.builder()
                    .abtest(this)
                    .displayName(requestVariable.getDisplayName())
                    .rate(requestVariable.getRate())
                    .name(requestVariable.getName())
                    .build();
                abtestVariables.add(newVariable);
            }
        });
        entityManager.flush();
    }

    private static void validateVariables(List<AbtestVariable> variables) {
        int sum = variables.stream().mapToInt(AbtestVariable::getRate).sum();
        if (sum != 100) {
            throw AbtestVariableIllegalArgumentException.withDetail("실험군 비율 합이 100이 아닙니다. rate sum: " + sum);
        }

        int distinctSize = variables.stream()
            .map(variable -> variable.getName())
            .distinct().toList().size();
        if (distinctSize != variables.size()) {
            throw AbtestVariableIllegalArgumentException.withDetail("실험군 간의 변수명(name)이 중복됩니다.");
        }
    }

    public String getWinnerName() {
        return winner != null ? winner.getName() : null;
    }

    public void close(String winnerName) {
        status = AbtestStatus.CLOSED;
        if (winnerName != null) {
            winner = getVariableByName(winnerName);
        }
        abtestVariables.forEach(AbtestVariable::close);
    }

    public void assignVariableByAdmin(AccessHistory accessHistory, String variableName) {
        resetExistVariable(accessHistory);
        AbtestVariable variable = getVariableByName(variableName);
        accessHistory.addVariable(variable);
        variable.addCount(1);
    }

    public AbtestVariable getVariableByName(String variableName) {
        return abtestVariables.stream()
            .filter(abtestVariable -> abtestVariable.getName().equals(variableName))
            .findAny()
            .orElseThrow(() -> AbtestNotIncludeVariableException.withDetail(
                "abtest name: " + title + ", winner name: " + variableName));
    }

    private void resetExistVariable(AccessHistory accessHistory) {
        Optional<AbtestVariable> variable = findVariableByAccessHistory(accessHistory);
        if (variable.isEmpty()) {
            return;
        }
        variable.get().addCount(-1);
        accessHistory.removeVariable(variable.get());
    }

    public Optional<AbtestVariable> findVariableByAccessHistory(AccessHistory accessHistory) {
        return accessHistory.getAccessHistoryAbtestVariables().stream()
            .filter(map -> map.getAccessHistory().getId().equals(accessHistory.getId()))
            .map(AccessHistoryAbtestVariable::getVariable)
            .findAny();
    }
}
