package in.koreatech.koin.domain.order.order.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_takeout_v2")
@NoArgsConstructor(access = PROTECTED)
public class OrderTakeout extends BaseEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @Size(max = 50)
    @Column(name = "to_owner", length = 50, updatable = false)
    private String toOwner;

    @NotNull
    @Column(name = "provide_cutlery", nullable = false, updatable = false, columnDefinition = "TINYINT(1)")
    private Boolean provideCutlery;

    @Column(name = "packaged_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime packagedAt;

    @Column(name = "estimated_packaged_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime estimatedPackagedAt;

    @Builder
    private OrderTakeout(
        Order order,
        String toOwner,
        Boolean provideCutlery,
        LocalDateTime packagedAt,
        LocalDateTime estimatedPackagedAt
    ) {
        this.order = order;
        this.toOwner = toOwner;
        this.provideCutlery = provideCutlery;
        this.packagedAt = packagedAt;
        this.estimatedPackagedAt = estimatedPackagedAt;
    }

    public void packaged() {
        this.packagedAt = LocalDateTime.now();
        this.order.packaged();
    }

    public void cooking() {
        this.estimatedPackagedAt = LocalDateTime.now();
        this.order.cooking();
    }

    public void pickedUp() {
        this.order.pickedUp();
    }
}
