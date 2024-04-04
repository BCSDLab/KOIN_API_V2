package in.koreatech.koin.domain.shop.model;

import java.time.LocalTime;

import org.hibernate.annotations.Where;

import in.koreatech.koin.global.config.LocalTimeAttributeConverter;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shop_opens")
@Where(clause = "is_deleted=0")
public class ShopOpen extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

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
    private Boolean isDeleted = false;

    @Builder
    private ShopOpen(Long id, Shop shop, String dayOfWeek, Boolean closed, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.shop = shop;
        this.dayOfWeek = dayOfWeek;
        this.closed = closed;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}
