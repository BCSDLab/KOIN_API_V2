package in.koreatech.koin.domain.order.model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_delivery")
@NoArgsConstructor(access = PROTECTED)
public class OrderDelivery extends BaseEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address", length = 100, nullable = false, updatable = false)
    private String address;

    @Size(max = 50)
    @Column(name = "to_owner", length = 50, updatable = false)
    private String toOwner;

    @Size(max = 50)
    @Column(name = "to_rider", length = 50, updatable = false)
    private String toRider;

    @NotNull
    @Column(name = "delivery_tip", nullable = false, updatable = false)
    private Integer deliveryTip;

    @NotNull
    @Column(name = "provide_cutlery", nullable = false, updatable = false, columnDefinition = "TINYINT(1)")
    private Boolean provideCutlery;

    @Column(name = "dispatched_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime dispatchedAt;

    @Column(name = "completed_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime completedAt;

    @Column(name = "estimated_arrival_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime estimatedArrivalAt;

    @Builder
    private OrderDelivery(
        Order order,
        String address,
        String toOwner,
        String toRider,
        Integer deliveryTip,
        Boolean provideCutlery,
        LocalDateTime dispatchedAt,
        LocalDateTime completedAt,
        LocalDateTime estimatedArrivalAt
    ) {
        this.order = order;
        this.address = address;
        this.toOwner = toOwner;
        this.toRider = toRider;
        this.deliveryTip = deliveryTip;
        this.provideCutlery = provideCutlery;
        this.dispatchedAt = dispatchedAt;
        this.completedAt = completedAt;
        this.estimatedArrivalAt = estimatedArrivalAt;
    }
}
