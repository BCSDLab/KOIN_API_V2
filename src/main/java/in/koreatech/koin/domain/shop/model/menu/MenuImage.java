package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_images", uniqueConstraints = {
    @UniqueConstraint(
        name = "SHOP_MENU_ID_AND_IMAGE_URL",
        columnNames = {"shop_menu_id", "image_url"}
    )}
)
@NoArgsConstructor(access = PROTECTED)
public class MenuImage {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    private MenuImage(Menu menu, String imageUrl) {
        this.menu = menu;
        this.imageUrl = imageUrl;
    }

    public void setMenu(Menu menu) {
        if (menu.equals(this.menu)) {
            return;
        }

        if (this.menu != null) {
            this.menu.getMenuImages().remove(this);
        }
        this.menu = menu;
        this.menu.getMenuImages().add(this);
    }
}
