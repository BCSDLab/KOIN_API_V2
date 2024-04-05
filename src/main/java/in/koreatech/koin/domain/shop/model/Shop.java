package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.REMOVE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.dto.ModifyShopRequest.InnerShopOpen;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shops")
@Where(clause = "is_deleted=0")
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    private Owner owner;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "internal_name", nullable = false, length = 50)
    private String internalName;

    @Size(max = 3)
    @Column(name = "chosung", length = 3)
    private String chosung;

    @Size(max = 50)
    @Column(name = "phone", length = 50)
    private String phone;

    @Lob
    @Column(name = "address")
    private String address;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "delivery", nullable = false)
    private Boolean delivery = false;

    @NotNull
    @Column(name = "delivery_price", nullable = false)
    private Long deliveryPrice;

    @NotNull
    @Column(name = "pay_card", nullable = false)
    private Boolean payCard = false;

    @NotNull
    @Column(name = "pay_bank", nullable = false)
    private Boolean payBank = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @NotNull
    @Column(name = "is_event", nullable = false)
    private Boolean isEvent = false;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    @NotNull
    @Column(name = "hit", nullable = false)
    private Long hit;

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopCategoryMap> shopCategories = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopOpen> shopOpens = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopImage> shopImages = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @Builder
    private Shop(
        Owner owner,
        String name,
        String internalName,
        String chosung,
        String phone,
        String address,
        String description,
        Boolean delivery,
        Long deliveryPrice,
        Boolean payCard,
        Boolean payBank,
        Boolean isDeleted,
        Boolean isEvent,
        String remarks,
        Long hit,
        List<ShopCategoryMap> shopCategories,
        List<ShopOpen> shopOpens,
        List<ShopImage> shopImages,
        List<MenuCategory> menuCategories
    ) {
        this.owner = owner;
        this.name = name;
        this.internalName = internalName;
        this.chosung = chosung;
        this.phone = phone;
        this.address = address;
        this.description = description;
        this.delivery = delivery;
        this.deliveryPrice = deliveryPrice;
        this.payCard = payCard;
        this.payBank = payBank;
        this.isDeleted = isDeleted;
        this.isEvent = isEvent;
        this.remarks = remarks;
        this.hit = hit;
        this.shopCategories = shopCategories;
        this.shopOpens = shopOpens;
        this.shopImages = shopImages;
        this.menuCategories = menuCategories;
    }

    public void modifyShop(
        String name,
        String phone,
        String address,
        String description,
        Boolean delivery,
        Long deliveryPrice,
        Boolean payCard,
        Boolean payBank
    ) {
        this.address = address;
        this.delivery = delivery;
        this.deliveryPrice = deliveryPrice;
        this.description = description;
        this.name = name;
        this.payBank = payBank;
        this.payCard = payCard;
        this.phone = phone;
    }

    public void modifyShopImages(List<String> imageUrls, EntityManager em) {
        this.shopImages.clear();
        em.flush();
        for (String imageUrl : imageUrls) {
            ShopImage shopImage = ShopImage.builder()
                .shop(this)
                .imageUrl(imageUrl)
                .build();
            this.shopImages.add(shopImage);
        }
    }

    public void modifyShopOpens(List<InnerShopOpen> innerShopOpens, EntityManager em) {
        this.shopOpens.clear();
        em.flush();
        for (var open : innerShopOpens) {
            ShopOpen shopOpen = open.toEntity(this);
            this.shopOpens.add(shopOpen);
        }
    }

    public void modifyShopCategories(List<ShopCategory> shopCategories, EntityManager em) {
        this.shopCategories.clear();
        em.flush();
        for (ShopCategory shopCategory : shopCategories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shop(this)
                .shopCategory(shopCategory)
                .build();
            this.shopCategories.add(shopCategoryMap);
        }
    }
}
