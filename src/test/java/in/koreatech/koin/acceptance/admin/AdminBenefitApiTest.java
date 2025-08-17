package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryMapAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryMapRepository;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;

public class AdminBenefitApiTest extends AcceptanceTest {

    @Autowired
    AdminBenefitCategoryRepository adminBenefitCategoryRepository;

    @Autowired
    AdminBenefitCategoryMapRepository adminBenefitCategoryMapRepository;

    @Autowired
    BenefitCategoryAcceptanceFixture benefitCategoryFixture;

    @Autowired
    BenefitCategoryMapAcceptanceFixture benefitCategoryMapFixture;

    @Autowired
    ShopAcceptanceFixture shopFixture;

    @Autowired
    UserAcceptanceFixture userFixture;

    @Autowired
    ShopParentCategoryAcceptanceFixture shopParentCategoryFixture;

    @Autowired
    ShopCategoryAcceptanceFixture shopCategoryFixture;

    @Autowired
    ShopNotificationMessageAcceptanceFixture shopNotificationMessageFixture;

    @Autowired
    private TransactionTemplate transactionTemplate;

    Admin admin;
    String token_admin;
    Owner 현수_사장님;

    BenefitCategory 배달비_무료;
    BenefitCategory 최소주문금액_무료;
    BenefitCategory 서비스_증정;
    BenefitCategory 가게까지_픽업;

    BenefitCategoryMap 김밥천국_혜택;
    BenefitCategoryMap 마슬랜_혜택;
    BenefitCategoryMap 티바_혜택;
    BenefitCategoryMap 신전_혜택;

    Shop 마슬랜;
    Shop 김밥천국;
    Shop 영업중인_티바;
    Shop 영업중이_아닌_신전_떡볶이;

    private ShopParentCategory shopParentCategory_가게;
    private ShopNotificationMessage notificationMessage_가게;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;

    @BeforeAll
    void setup() {
        clear();
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin.getUser());

        배달비_무료 = benefitCategoryFixture.배달비_무료();
        최소주문금액_무료 = benefitCategoryFixture.최소주문금액_무료();
        서비스_증정 = benefitCategoryFixture.서비스_증정();
        가게까지_픽업 = benefitCategoryFixture.가게까지_픽업();

        마슬랜 = shopFixture.마슬랜(현수_사장님, shopCategory_치킨);
        김밥천국 = shopFixture.김밥천국(현수_사장님, shopCategory_일반);
        영업중인_티바 = shopFixture.영업중인_티바(현수_사장님);
        영업중이_아닌_신전_떡볶이 = shopFixture.영업중이_아닌_신전_떡볶이(현수_사장님);

        김밥천국_혜택 = benefitCategoryMapFixture.설명이_포함된_혜택_추가(김밥천국, 배달비_무료, "설명1");
        마슬랜_혜택 = benefitCategoryMapFixture.설명이_포함된_혜택_추가(마슬랜, 배달비_무료, "설명2");
        티바_혜택 = benefitCategoryMapFixture.설명이_포함된_혜택_추가(영업중인_티바, 배달비_무료, "설명3");
        신전_혜택 = benefitCategoryMapFixture.설명이_포함된_혜택_추가(영업중이_아닌_신전_떡볶이, 배달비_무료, "설명4");

        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
    }

    @Test
    void 혜택_카테고리를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/benefit/categories")
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "benefits": [
                        {
                          "id": 1,
                          "title": "배달비 아끼기",
                          "detail": "계좌이체하면 배달비가 무료(할인)인 상점들을 모아뒀어요.",
                          "on_image_url": "https://stage-static.koreatech.in/shop/benefit/deliveryOn.png",
                          "off_image_url": "https://stage-static.koreatech.in/shop/benefit/deliveryOff.png"
                        },
                        {
                          "id": 2,
                          "title": "최소주문금액 무료",
                          "detail": "계좌이체하면 최소주문금액이 무료인 상점들을 모아뒀어요.",
                          "on_image_url": "https://stage-static.koreatech.in/shop/benefit/paidOn.png",
                          "off_image_url": "https://stage-static.koreatech.in/shop/benefit/paidOff.png"
                        },
                        {
                          "id": 3,
                          "title": "서비스 증정",
                          "detail": "계좌이체하면 서비스를 주는 상점들을 모아뒀어요.",
                          "on_image_url": "https://stage-static.koreatech.in/shop/benefit/serviceOn.png",
                          "off_image_url": "https://stage-static.koreatech.in/shop/benefit/serviceOff.png"
                        },
                        {
                          "id": 4,
                          "title": "가게까지 픽업",
                          "detail": "사장님께서 직접 가게까지 데려다주시는 상점들을 모아뒀어요.",
                          "on_image_url": "https://stage-static.koreatech.in/shop/benefit/pickUpOn.png",
                          "off_image_url": "https://stage-static.koreatech.in/shop/benefit/pickUpOff.png"
                        }
                      ]
                    }
                """));
    }

    @Test
    void 혜택_카테고리를_추가한다() throws Exception {
        mockMvc.perform(
                post("/admin/benefit/categories")
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "title": "엄청난 혜택",
                              "detail": "밥이 무료인 상점들을 모아뒀어요.",
                              "on_image_url": "https://example.com/exampleOn.jpg",
                              "off_image_url": "https://example.com/exampleOff.jpg"
                            }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(content().json("""
                    {
                      "id": 5,
                      "title": "엄청난 혜택",
                      "detail": "밥이 무료인 상점들을 모아뒀어요.",
                      "on_image_url": "https://example.com/exampleOn.jpg",
                      "off_image_url": "https://example.com/exampleOff.jpg"
                    }
                """));
    }

    @Test
    void 혜택_카테고리를_수정한다() throws Exception {
        mockMvc.perform(
                put("/admin/benefit/categories/{id}", 배달비_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "title": "배달비 유료",
                              "detail": "배달비 유료인 상점들을 모아뒀어요.",
                              "on_image_url": "https://example.com/modifyOn.jpg",
                              "off_image_url": "https://example.com/modifyOff.jpg"
                            }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "id": 1,
                      "title": "배달비 유료",
                      "detail": "배달비 유료인 상점들을 모아뒀어요.",
                      "on_image_url": "https://example.com/modifyOn.jpg",
                      "off_image_url": "https://example.com/modifyOff.jpg"
                    }
                """));
    }

    @Test
    void 혜택_카테고리를_삭제한다() throws Exception {
        mockMvc.perform(
                delete("/admin/benefit/categories/{id}", 배달비_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isNoContent());

        assertThat(adminBenefitCategoryRepository.findById(배달비_무료.getId())).isNotPresent();
        assertThat(
            adminBenefitCategoryMapRepository
                .findAllByBenefitCategoryIdOrderByShopName(배달비_무료.getId())
        ).isEmpty();
    }

    @Test
    void 특정_혜택을_제공하는_모든_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/benefit/{id}/shops", 배달비_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                        {
                          "count": 4,
                          "shops": [
                            {
                              "shop_benefit_map_id": %d,
                              "id": %d,
                              "name": "김밥천국",
                              "detail": "설명1"
                            },
                            {
                              "shop_benefit_map_id": %d,
                              "id": %d,
                              "name": "마슬랜 치킨",
                              "detail": "설명2"
                            },
                            {
                              "shop_benefit_map_id": %d,
                              "id": %d,
                              "name": "티바",
                              "detail": "설명3"
                            },
                            {
                              "shop_benefit_map_id": %d,
                              "id": %d,
                              "name": "신전 떡볶이",
                              "detail": "설명4"
                            }
                          ]
                        }
                    """,
                김밥천국_혜택.getId(), 김밥천국.getId(),
                마슬랜_혜택.getId(), 마슬랜.getId(),
                티바_혜택.getId(), 영업중인_티바.getId(),
                신전_혜택.getId(), 영업중이_아닌_신전_떡볶이.getId())));
    }

    @Test
    void 특정_혜택을_제공하는_상점들을_추가한다() throws Exception {
        mockMvc.perform(
                post("/admin/benefit/{id}/shops", 최소주문금액_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                              "shop_details": [
                                {
                                  "shop_id": %d,
                                  "detail": "김밥혜택설명"
                                },
                                {
                                  "shop_id": %d,
                                  "detail": "마슬랜혜택설명"
                                }
                              ]
                            }
                        """, 김밥천국.getId(), 마슬랜.getId()))
            )
            .andExpect(status().isCreated())
            .andExpect(content().json(String.format("""
                    {
                      "shops": [
                        {
                          "id": %d,
                          "name": "김밥천국",
                          "detail": "김밥혜택설명"
                        },
                        {
                          "id": %d,
                          "name": "마슬랜 치킨",
                          "detail": "마슬랜혜택설명"
                        }
                      ]
                    }
                """, 김밥천국.getId(), 마슬랜.getId())));
    }

    @Test
    void 특정_혜택을_제공하는_상점들을_수정한다() throws Exception {
        mockMvc.perform(
                put("/admin/benefit")
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                              "modify_details": [
                                {
                                  "shop_benefit_map_id": %d,
                                  "detail": "김밥새혜택설명"
                                },
                                {
                                  "shop_benefit_map_id": %d,
                                  "detail": "마슬랜새혜택설명"
                                }
                              ]
                            }
                        """, 김밥천국_혜택.getId(), 마슬랜_혜택.getId()))
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            List<BenefitCategoryMap> updatedBenefit =
                adminBenefitCategoryMapRepository.findAllByIdIn(
                    List.of(김밥천국_혜택.getId(), 마슬랜_혜택.getId())
                );

            Map<Integer, String> details = updatedBenefit.stream()
                .collect(Collectors.toMap(
                    BenefitCategoryMap::getId,
                    BenefitCategoryMap::getDetail
                ));

            assertThat(details).isEqualTo(Map.of(
                김밥천국_혜택.getId(), "김밥새혜택설명",
                마슬랜_혜택.getId(), "마슬랜새혜택설명"
            ));
        });
    }

    @Test
    void 특정_혜택을_제공하는_상점들을_삭제한다() throws Exception {
        mockMvc.perform(
                delete("/admin/benefit/{id}/shops", 배달비_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                              "shop_ids": [%d, %d]
                            }
                        """, 김밥천국.getId(), 마슬랜.getId()))
            )
            .andExpect(status().isNoContent());

        List<BenefitCategoryMap> shops =
            adminBenefitCategoryMapRepository
                .findAllByBenefitCategoryIdOrderByShopName(배달비_무료.getId());

        assertThat(shops)
            .extracting("shop.id")
            .containsExactly(
                영업중이_아닌_신전_떡볶이.getId(),
                영업중인_티바.getId()
            );
    }

    @Test
    void 상점을_검색한다() throws Exception {
        mockMvc.perform(
                get("/admin/benefit/{id}/shops/search", 배달비_무료.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .param("search_keyword", "김밥")
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                  "benefit_shops": [
                    {
                      "id": %d,
                      "name": "김밥천국"
                    }
                  ],
                  "non_benefit_shops": []
                }
                """, 김밥천국.getId())));
    }
}
