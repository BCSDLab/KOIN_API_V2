package in.koreatech.koin.domain.dining.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dinings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Dining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

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

    @Builder
    private Dining(Integer id, String date, String type, String place, Integer priceCard, Integer priceCash,
        Integer kcal, String menu) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.place = place;
        this.priceCard = priceCard;
        this.priceCash = priceCash;
        this.kcal = kcal;
        this.menu = menu;
    }
}
