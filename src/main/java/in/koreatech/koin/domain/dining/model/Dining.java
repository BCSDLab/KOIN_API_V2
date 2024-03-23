package in.koreatech.koin.domain.dining.model;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dining extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "place", nullable = false)
    private String place;

    @Column(name = "price_card")
    private Integer priceCard;

    @Column(name = "price_cash")
    private Integer priceCash;

    @Column(name = "kcal")
    private Integer kcal;

    @Column(name = "menu", nullable = false)
    private String menu;

    @NotNull
    @Column(name = "sold_out", nullable = false)
    private Boolean soldOut = false;

    @Builder
    private Dining(Long id, String date, String type, String place, Integer priceCard, Integer priceCash,
        Integer kcal, String menu, Boolean soldOut) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.place = place;
        this.priceCard = priceCard;
        this.priceCash = priceCash;
        this.kcal = kcal;
        this.menu = menu;
        this.soldOut = soldOut;
    }
}
