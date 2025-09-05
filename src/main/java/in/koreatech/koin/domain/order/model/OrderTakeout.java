package in.koreatech.koin.domain.order.model;

import static lombok.AccessLevel.PROTECTED;

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
@Table(name = "order_takeout")
@NoArgsConstructor(access = PROTECTED)
public class OrderTakeout {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String id;

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

    @Builder
    private OrderTakeout(
        Order order,
        String toOwner,
        Boolean provideCutlery
    ) {
        this.order = order;
        this.toOwner = toOwner;
        this.provideCutlery = provideCutlery;
    }
}
