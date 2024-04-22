package in.koreatech.koin.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.domain.shop.repository.ShopRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopFixture {

    private final ShopRepository shopRepository;

    public ShopFixture(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop 마슬랜(Owner owner) {
        return shopRepository.save(
            Shop.builder()
                .owner(owner)
                .name("마슬랜 치킨")
                .internalName("마슬랜")
                .chosung("마")
                .phone("010-7574-1212")
                .address("천안시 동남구 병천면 1600")
                .description("마슬랜 치킨입니다.")
                .delivery(true)
                .deliveryPrice(3000)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고")
                .hit(0)
                .build()
        );
    }

    public Shop 신전_떡볶이(Owner owner) {
        return shopRepository.save(
            Shop.builder()
                .owner(owner)
                .name("신전 떡볶이")
                .internalName("신전")
                .chosung("신")
                .phone("010-7788-9900")
                .address("천안시 동남구 병천면 1600 신전떡볶이")
                .description("신전떡볶이입니다.")
                .delivery(true)
                .deliveryPrice(2000)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고")
                .hit(0)
                .build()
        );
    }

    public ShopFixtureBuilder builder() {
        return new ShopFixtureBuilder();
    }

    public final class ShopFixtureBuilder {

        private Owner owner;
        private String name;
        private String internalName;
        private String chosung;
        private String phone;
        private String address;
        private String description;
        private boolean delivery;
        private Integer deliveryPrice;
        private boolean payCard;
        private boolean payBank;
        private boolean isDeleted;
        private boolean isEvent;
        private String remarks;
        private Integer hit;
        private List<ShopCategoryMap> shopCategories;
        private List<ShopOpen> shopOpens;
        private List<ShopImage> shopImages;
        private List<MenuCategory> menuCategories;

        public ShopFixtureBuilder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public ShopFixtureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ShopFixtureBuilder internalName(String internalName) {
            this.internalName = internalName;
            return this;
        }

        public ShopFixtureBuilder chosung(String chosung) {
            this.chosung = chosung;
            return this;
        }

        public ShopFixtureBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public ShopFixtureBuilder address(String address) {
            this.address = address;
            return this;
        }

        public ShopFixtureBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ShopFixtureBuilder delivery(boolean delivery) {
            this.delivery = delivery;
            return this;
        }

        public ShopFixtureBuilder deliveryPrice(Integer deliveryPrice) {
            this.deliveryPrice = deliveryPrice;
            return this;
        }

        public ShopFixtureBuilder payCard(boolean payCard) {
            this.payCard = payCard;
            return this;
        }

        public ShopFixtureBuilder payBank(boolean payBank) {
            this.payBank = payBank;
            return this;
        }

        public ShopFixtureBuilder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public ShopFixtureBuilder isEvent(boolean isEvent) {
            this.isEvent = isEvent;
            return this;
        }

        public ShopFixtureBuilder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public ShopFixtureBuilder hit(Integer hit) {
            this.hit = hit;
            return this;
        }

        public ShopFixtureBuilder shopCategories(List<ShopCategoryMap> shopCategories) {
            this.shopCategories = shopCategories;
            return this;
        }

        public ShopFixtureBuilder shopOpens(List<ShopOpen> shopOpens) {
            this.shopOpens = shopOpens;
            return this;
        }

        public ShopFixtureBuilder shopImages(List<ShopImage> shopImages) {
            this.shopImages = shopImages;
            return this;
        }

        public ShopFixtureBuilder menuCategories(List<MenuCategory> menuCategories) {
            this.menuCategories = menuCategories;
            return this;
        }

        public Shop build() {
            return shopRepository.save(
                Shop.builder()
                    .shopOpens(shopOpens)
                    .menuCategories(menuCategories)
                    .description(description)
                    .owner(owner)
                    .phone(phone)
                    .address(address)
                    .payCard(payCard)
                    .isDeleted(isDeleted)
                    .isEvent(isEvent)
                    .delivery(delivery)
                    .hit(hit)
                    .shopCategories(shopCategories)
                    .internalName(internalName)
                    .shopImages(shopImages)
                    .name(name)
                    .chosung(chosung)
                    .deliveryPrice(deliveryPrice)
                    .remarks(remarks)
                    .payBank(payBank)
                    .build()
            );
        }
    }
}
