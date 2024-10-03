package in.koreatech.koin.domain.coopshop.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coop_opens")
@NoArgsConstructor(access = PROTECTED)
public class CoopOpen {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coop_id", referencedColumnName = "id", nullable = false)
    private CoopShop coopShop;

    @Column(name = "type")
    private String type;

    @NotNull
    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "open_time")
    private String openTime;

    @Column(name = "close_time")
    private String closeTime;

    @Builder
    private CoopOpen(
        CoopShop coopShop,
        String type,
        String dayOfWeek,
        String openTime,
        String closeTime
    ) {
        this.coopShop = coopShop;
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

}
