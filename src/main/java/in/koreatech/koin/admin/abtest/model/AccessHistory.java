package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@Table(name = "access_history", schema = "koin")
public class AccessHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Size(max = 45)
    @NotNull
    @Column(name = "public_ip", nullable = false, length = 45)
    private String publicIp;

    @OneToMany(mappedBy = "accessHistory", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AccessHistoryAbtestVariable> accessHistoryAbtestVariables = new ArrayList<>();

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
        String publicIp
    ) {
        this.id = id;
        this.device = device;
        this.publicIp = publicIp;
    }

    public void connectDevice(Device device) {
        this.device = device;
    }

    public void addVariable(AbtestVariable variable) {
        accessHistoryAbtestVariables.add(AccessHistoryAbtestVariable.builder()
            .accessHistory(this)
            .variable(variable)
            .build());
    }

    public void addAbtestVariable(AbtestVariable variable) {
        if (!hasVariable(variable.getId())) {
            AccessHistoryAbtestVariable saved = AccessHistoryAbtestVariable.builder()
                .accessHistory(this)
                .variable(variable)
                .build();
            this.accessHistoryAbtestVariables.add(saved);
            variable.getAccessHistoryAbtestVariables().add(saved);
        }
    }

    public boolean hasVariable(Integer variableId) {
        return accessHistoryAbtestVariables.stream()
            .map(AccessHistoryAbtestVariable::getVariable)
            .anyMatch(abtestVariable -> Objects.equals(abtestVariable.getId(), variableId));
    }
}
