package in.koreatech.koin.domain.dining.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "dining_menus", uniqueConstraints = {
    @UniqueConstraint(
        name = "ux_date_type_place",
        columnNames = {"date", "type", "place"}
    )
})
public class Dining extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Enumerated(value = STRING)
    @Column(name = "type", nullable = false)
    private DiningType type;

    @NotNull
    @Column(name = "place", nullable = false)
    private String place;

    @Column(name = "price_card")
    private Integer priceCard;

    @Column(name = "price_cash")
    private Integer priceCash;

    @Column(name = "kcal")
    private Integer kcal;

    @NotNull
    @Column(name = "menu", nullable = false)
    private String menu;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sold_out", columnDefinition = "DATETIME")
    private LocalDateTime soldOut;

    @Column(name = "is_changed", columnDefinition = "DATETIME")
    private LocalDateTime isChanged;

    @Column(name = "likes")
    private Integer likes = 0;

    @Builder
    private Dining(
        LocalDate date,
        DiningType type,
        String place,
        Integer priceCard,
        Integer priceCash,
        Integer kcal,
        String menu,
        String imageUrl,
        LocalDateTime soldOut,
        LocalDateTime isChanged,
        Integer likes
    ) {
        this.date = date;
        this.type = type;
        this.place = place;
        this.priceCard = priceCard;
        this.priceCash = priceCash;
        this.kcal = kcal;
        this.menu = menu;
        this.imageUrl = imageUrl;
        this.soldOut = soldOut;
        this.isChanged = isChanged;
        this.likes = likes;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSoldOut(LocalDateTime soldout) {
        this.soldOut = soldout;
    }

    public void cancelSoldOut() {
        this.soldOut = null;
    }

    public void likesDining() {
        this.likes++;
    }

    public void likesDiningCancel() {
        this.likes--;
    }

    /**
     * DB에 "[메뉴, 메뉴, ...]" 형태로 저장되어 List로 파싱하여 반환
     */
    public List<String> getMenu() {
        if (menu == null || menu.isBlank()) {
            throw new KoinIllegalStateException("메뉴가 잘못된 형태로 저장되어있습니다.", menu);
        }
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(menu);
        List<String> parsedMenu = new ArrayList<>();
        while (matcher.find()) {
            parsedMenu.add(matcher.group(1));
        }
        return parsedMenu;
    }
}
