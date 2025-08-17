package in.koreatech.koin.domain.shop.model.shop;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_images",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "SHOP_ID_AND_IMAGE_URL",
            columnNames = {"shop_id", "image_url"}
        )
    }
)
public class ShopImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    private ShopImage(Shop shop, String imageUrl) {
        this.shop = shop;
        this.imageUrl = imageUrl;
    }
}
