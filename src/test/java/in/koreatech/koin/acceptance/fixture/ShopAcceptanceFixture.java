package in.koreatech.koin.acceptance.fixture;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopAcceptanceFixture {

    private final ShopRepository shopRepository;

    public ShopAcceptanceFixture(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop 김밥천국(Owner owner, ShopCategory shopMainCategory) {
        var shop = shopRepository.save(
            Shop.builder()
                .owner(owner)
                .shopMainCategory(shopMainCategory)
                .name("김밥천국")
                .internalName("김천")
                .chosung("김")
                .phone("010-7574-1212")
                .address("천안시 동남구 병천면 1600")
                .description("김밥천국입니다.")
                .delivery(true)
                .deliveryPrice(3000)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고")
                .hit(0)
                .bank("국민")
                .accountNumber("01022595923")
                .build()
        );
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/김천1.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/김천2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(21, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
    }

    public Shop 마슬랜(Owner owner, ShopCategory shopMainCategory) {
        var shop = shopRepository.save(
            Shop.builder()
                .owner(owner)
                .shopMainCategory(shopMainCategory)
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
                .bank("국민")
                .accountNumber("01022595923")
                .build()
        );
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/마슬랜.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/마슬랜2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(21, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
    }

    public Shop 영업중이_아닌_신전_떡볶이(Owner owner) {
        var shop = shopRepository.save(
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
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SUNDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(12, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("TUESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("WEDNESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("THURSDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SATURDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
    }

    public Shop 배달_안되는_신전_떡볶이(Owner owner) {
        var shop = shopRepository.save(
            Shop.builder()
                .owner(owner)
                .name("신전 떡볶이")
                .internalName("신전")
                .chosung("신")
                .phone("010-7788-9900")
                .address("천안시 동남구 병천면 1600 신전떡볶이")
                .description("신전떡볶이입니다.")
                .delivery(false)
                .deliveryPrice(2000)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고")
                .hit(0)
                .build()
        );
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SUNDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("TUESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("WEDNESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("THURSDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SATURDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
    }

    public Shop 영업중인_티바(Owner owner) {
        var shop = shopRepository.save(
            Shop.builder()
                .owner(owner)
                .name("티바")
                .internalName("티바")
                .chosung("티")
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
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SUNDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("TUESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("WEDNESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("THURSDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(11, 30))
                    .closeTime(LocalTime.of(21, 30))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SATURDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
    }

    public Shop _24시간_영업중인_티바(Owner owner) {
        var shop = shopRepository.save(
            Shop.builder()
                .owner(owner)
                .name("티바")
                .internalName("티바")
                .chosung("티")
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
        shop.getShopImages().addAll(
            List.of(
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전.png")
                    .build(),
                ShopImage.builder()
                    .shop(shop)
                    .imageUrl("https://test-image.com/신전2.png")
                    .build()
            )
        );
        shop.getShopOpens().addAll(
            List.of(
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SUNDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("MONDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("TUESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("WEDNESDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("THURSDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("FRIDAY")
                    .build(),
                ShopOpen.builder()
                    .openTime(LocalTime.of(0, 0))
                    .closeTime(LocalTime.of(0, 0))
                    .shop(shop)
                    .closed(false)
                    .dayOfWeek("SATURDAY")
                    .build()
            )
        );
        return shopRepository.save(shop);
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
        private List<ShopCategoryMap> shopCategories = new ArrayList<>();
        private List<ShopOpen> shopOpens = new ArrayList<>();
        private List<ShopImage> shopImages = new ArrayList<>();
        private List<MenuCategory> menuCategories = new ArrayList<>();

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
            var shop = shopRepository.save(
                Shop.builder()
                    .description(description)
                    .owner(owner)
                    .phone(phone)
                    .address(address)
                    .payCard(payCard)
                    .isDeleted(isDeleted)
                    .isEvent(isEvent)
                    .delivery(delivery)
                    .hit(hit)
                    .internalName(internalName)
                    .name(name)
                    .chosung(chosung)
                    .deliveryPrice(deliveryPrice)
                    .remarks(remarks)
                    .payBank(payBank)
                    .build()
            );
            shop.getShopOpens().addAll(shopOpens);
            shop.getShopImages().addAll(shopImages);
            shop.getMenuCategories().addAll(menuCategories);
            shop.getShopCategories().addAll(shopCategories);
            return shopRepository.save(shop);
        }
    }
}
