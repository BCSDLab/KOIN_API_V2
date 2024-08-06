package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;

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

    //TODO 연결테이블 적용
/*    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_id", nullable = false)
    private AbtestVariable variable;*/

    @Builder
    private AccessHistory(
        Integer id,
        Device device,
        String publicIp,
        AbtestVariable variable
    ) {
        this.id = id;
        this.device = device;
        this.publicIp = publicIp;
        this.variable = variable;
    }
}
