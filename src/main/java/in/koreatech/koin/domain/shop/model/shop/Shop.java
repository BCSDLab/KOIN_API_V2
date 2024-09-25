package in.koreatech.koin.domain.shop.model.shop;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.annotations.Where;

import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.dto.shop.ModifyShopRequest.InnerShopOpen;
import in.koreatech.koin.domain.shop.model.article.EventArticle;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

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

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "delivery", nullable = false)
    private Boolean delivery = false;

    @NotNull
    @Column(name = "delivery_price", nullable = false)
    @PositiveOrZero
    private Integer deliveryPrice;

    @NotNull
    @Column(name = "pay_card", nullable = false)
    private boolean payCard = false;

    @NotNull
    @Column(name = "pay_bank", nullable = false)
    private boolean payBank = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @Column(name = "is_event", nullable = false)
    private boolean isEvent = false;

    @Column(name = "remarks")
    private String remarks;

    @NotNull
    @Column(name = "hit", nullable = false)
    private Integer hit;

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private Set<ShopCategoryMap> shopCategories = new HashSet<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopOpen> shopOpens = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopImage> shopImages = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<EventArticle> eventArticles = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopReview> reviews = new ArrayList<>();

    @Size(max = 10)
    @Column(name = "bank", length = 10)
    private String bank;

    @Size(max = 20)
    @Column(name = "accountNumber", length = 20)
    private String accountNumber;

    @Builder
    private Shop(
        Owner owner,
        String name,
        String internalName,
        String chosung,
        String phone,
        String address,
        String description,
        boolean delivery,
        Integer deliveryPrice,
        boolean payCard,
        boolean payBank,
        boolean isDeleted,
        boolean isEvent,
        String remarks,
        Integer hit,
        String bank,
        String accountNumber
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
        this.bank = bank;
        this.accountNumber = accountNumber;
    }

    public void modifyShop(
        String name,
        String phone,
        String address,
        String description,
        boolean delivery,
        Integer deliveryPrice,
        Boolean payCard,
        boolean payBank,
        String bank,
        String accountNumber
    ) {
        this.address = address;
        this.delivery = delivery;
        this.deliveryPrice = deliveryPrice;
        this.description = description;
        this.name = name;
        this.payBank = payBank;
        this.payCard = payCard;
        this.phone = phone;
        this.bank = bank;
        this.accountNumber = accountNumber;
    }

    public void modifyShopImages(List<String> imageUrls, EntityManager entityManager) {
        this.shopImages.clear();
        entityManager.flush();
        for (String imageUrl : imageUrls) {
            ShopImage shopImage = ShopImage.builder().shop(this).imageUrl(imageUrl).build();
            this.shopImages.add(shopImage);
        }
    }

    public void modifyShopOpens(List<InnerShopOpen> innerShopOpens, EntityManager entityManager) {
        this.shopOpens.clear();
        entityManager.flush();
        for (var open : innerShopOpens) {
            ShopOpen shopOpen = open.toEntity(this);
            this.shopOpens.add(shopOpen);
        }
    }

    public void modifyAdminShopOpens(
        List<AdminModifyShopRequest.InnerShopOpen> innerShopOpens,
        EntityManager entityManager
    ) {
        this.shopOpens.clear();
        entityManager.flush();
        for (var open : innerShopOpens) {
            ShopOpen shopOpen = open.toEntity(this);
            this.shopOpens.add(shopOpen);
        }
    }

    public void modifyShopCategories(List<ShopCategory> shopCategories, EntityManager entityManager) {
        this.shopCategories.clear();
        entityManager.flush();
        for (ShopCategory shopCategory : shopCategories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder().shop(this).shopCategory(shopCategory).build();
            this.shopCategories.add(shopCategoryMap);
        }
    }

    public boolean isOpen(LocalDateTime now) {
        String currDayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US).toUpperCase();
        String prevDayOfWeek = now.minusDays(1).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US).toUpperCase();
        for (ShopOpen shopOpen : shopOpens) {
            if (shopOpen.isClosed()) {
                continue;
            }
            if (shopOpen.getDayOfWeek().equals(currDayOfWeek) && isBetweenDate(now, shopOpen, now.toLocalDate())) {
                return true;
            }
            if (
                shopOpen.getDayOfWeek().equals(prevDayOfWeek) && isBetweenDate(now, shopOpen,
                    now.minusDays(1).toLocalDate())
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean isBetweenDate(LocalDateTime now, ShopOpen shopOpen, LocalDate criteriaDate) {
        LocalDateTime start = LocalDateTime.of(criteriaDate, shopOpen.getOpenTime());
        LocalDateTime end = LocalDateTime.of(criteriaDate, shopOpen.getCloseTime());
        if (!shopOpen.getCloseTime().isAfter(shopOpen.getOpenTime())) {
            end = end.plusDays(1);
        }
        return !start.isAfter(now) && !end.isBefore(now);
    }

    public void updateOwner(Owner owner) {
        this.owner = owner;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void cancelDelete() {
        this.isDeleted = false;
    }
}
