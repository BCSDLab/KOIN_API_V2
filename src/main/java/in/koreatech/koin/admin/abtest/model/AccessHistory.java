package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.annotation.CreatedDate;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "access_history", schema = "koin")
public class AccessHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @OneToMany(mappedBy = "accessHistory", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccessHistoryAbtestVariable> accessHistoryAbtestVariables = new ArrayList<>();

    @NotNull
    @Column(name = "last_accessed_at", nullable = false, columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime lastAccessedAt;

    public Optional<AbtestVariable> findVariableByAbtestId(int abtestId) {
        return accessHistoryAbtestVariables.stream()
            .map(AccessHistoryAbtestVariable::getVariable)
            .filter(abtestVariable -> abtestVariable.getAbtest().getId().equals(abtestId))
            .findAny();
    }

    @Builder
    private AccessHistory(
        Integer id,
        Device device,
        LocalDateTime lastAccessedAt
    ) {
        this.id = id;
        this.device = device;
        this.lastAccessedAt = lastAccessedAt;
    }

    public static AccessHistory create() {
        return AccessHistory.builder()
            .lastAccessedAt(LocalDateTime.now())
            .build();
    }

    public void connectDevice(Device device) {
        this.device = device;
        device.setAccessHistory(this);
    }

    public void addVariable(AbtestVariable variable) {
        accessHistoryAbtestVariables.add(AccessHistoryAbtestVariable.builder()
            .accessHistory(this)
            .variable(variable)
            .build());
    }

    public List<AbtestVariable> getVariableBy(Abtest abtest) {
        return accessHistoryAbtestVariables.stream()
            .map(AccessHistoryAbtestVariable::getVariable)
            .filter(abtestVariable -> abtestVariable.getAbtest().equals(abtest))
            .toList();
    }

    public void addAbtestVariable(AbtestVariable variable) {
        accessHistoryAbtestVariables.removeIf(map -> map.getVariable().getAbtest().equals(variable.getAbtest()));

        AccessHistoryAbtestVariable saved = AccessHistoryAbtestVariable.builder()
            .accessHistory(this)
            .variable(variable)
            .build();
        accessHistoryAbtestVariables.add(saved);
        variable.getAccessHistoryAbtestVariables().add(saved);
    }

    public boolean hasVariable(Integer variableId) {
        return accessHistoryAbtestVariables.stream()
            .map(AccessHistoryAbtestVariable::getVariable)
            .anyMatch(abtestVariable -> Objects.equals(abtestVariable.getId(), variableId));
    }

    public void removeVariable(AbtestVariable variable) {
        accessHistoryAbtestVariables.removeIf(
            accessHistoryAbtestVariable -> accessHistoryAbtestVariable.getVariable().equals(variable)
        );
    }

    // TODO: 각 로그인 및 리프레시 요청에 추가 필요
    public void updateLastAccessedAt(Clock clock) {
        this.lastAccessedAt = LocalDateTime.now(clock);
    }
}
