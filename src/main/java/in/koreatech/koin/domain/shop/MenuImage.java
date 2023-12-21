package in.koreatech.koin.domain.shop;

import jakarta.persistence.Column;
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
@Table(name = "shop_menu_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuImage {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    private MenuImage(String imageUrl) {
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
