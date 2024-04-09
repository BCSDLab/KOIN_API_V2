package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalTime;

import org.hibernate.annotations.Where;

import in.koreatech.koin.global.config.LocalTimeAttributeConverter;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_opens")
@Where(clause = "is_deleted=0")
public class ShopOpen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @Size(max = 10)
    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @NotNull
    @Column(name = "closed", nullable = false)
    private Boolean closed;

    @NotNull
    @Column(name = "open_time")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime openTime;

    @NotNull
    @Column(name = "close_time")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime closeTime;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private ShopOpen(Shop shop, String dayOfWeek, boolean closed, LocalTime openTime, LocalTime closeTime) {
        this.shop = shop;
        this.dayOfWeek = dayOfWeek;
        this.closed = closed;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}
