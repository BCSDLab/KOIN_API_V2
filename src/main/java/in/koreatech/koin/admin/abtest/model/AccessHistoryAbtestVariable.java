package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "access_history_abtest_variable", schema = "koin")
public class AccessHistoryAbtestVariable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "access_history_id", nullable = false)
    private AccessHistory accessHistory;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_id", nullable = false)
    private AbtestVariable variable;

    @Builder
    private AccessHistoryAbtestVariable(
        Integer id,
        AccessHistory accessHistory,
        AbtestVariable variable
    ) {
        this.id = id;
        this.accessHistory = accessHistory;
        this.variable = variable;
    }
}
