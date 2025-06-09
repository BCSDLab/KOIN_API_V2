package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryFixture;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryMapFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryFixture;
import in.koreatech.koin.acceptance.fixture.ShopFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewFixture;
import in.koreatech.koin.acceptance.fixture.UserFixture;

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BenefitApiTest extends AcceptanceTest {

    @Autowired
    BenefitCategoryFixture benefitCategoryFixture;

    @Autowired
    BenefitCategoryMapFixture benefitCategoryMapFixture;

    @Autowired
    ShopReviewFixture shopReviewFixture;

    @Autowired
    ShopFixture shopFixture;

    @Autowired
    UserFixture userFixture;

    @Autowired
    DepartmentFixture departmentFixture;

    @Autowired
    ShopCategoryFixture shopCategoryFixture;

    @Autowired
    ShopParentCategoryFixture shopParentCategoryFixture;

    @Autowired
    ShopNotificationMessageFixture shopNotificationMessageFixture;

    BenefitCategory 배달비_무료;
    BenefitCategory 최소주문금액_무료;
    BenefitCategory 서비스_증정;
    BenefitCategory 가게까지_픽업;

    Owner 현수_사장님;

    Student 성빈_학생;
    Department 컴퓨터_공학부;

    Shop 마슬랜;
    Shop 김밥천국;
    Shop 영업중인_티바;
    Shop 영업중이_아닌_신전_떡볶이;

    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private ShopParentCategory shopParentCategory_가게;
    private ShopNotificationMessage notificationMessage_가게;

    @BeforeAll
    void setup() {
        clear();
        현수_사장님 = userFixture.현수_사장님();
        배달비_무료 = benefitCategoryFixture.배달비_무료();
        최소주문금액_무료 = benefitCategoryFixture.최소주문금액_무료();
        서비스_증정 = benefitCategoryFixture.서비스_증정();
        가게까지_픽업 = benefitCategoryFixture.가게까지_픽업();

        마슬랜 = shopFixture.마슬랜(현수_사장님, shopCategory_치킨);
        김밥천국 = shopFixture.김밥천국(현수_사장님, shopCategory_일반);
        영업중인_티바 = shopFixture.영업중인_티바(현수_사장님);
        영업중이_아닌_신전_떡볶이 = shopFixture.영업중이_아닌_신전_떡볶이(현수_사장님);

        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
        shopCategory_일반 = shopCategoryFixture.카테고리_일반음식(shopParentCategory_가게);

        성빈_학생 = userFixture.성빈_학생(departmentFixture.컴퓨터공학부());

        benefitCategoryMapFixture.설명이_포함된_혜택_추가(김밥천국, 배달비_무료, "무료");
        benefitCategoryMapFixture.혜택_추가(마슬랜, 배달비_무료);
        benefitCategoryMapFixture.혜택_추가(영업중인_티바, 배달비_무료);
        benefitCategoryMapFixture.혜택_추가(영업중이_아닌_신전_떡볶이, 배달비_무료);

        shopReviewFixture.리뷰_4점(성빈_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(성빈_학생, 영업중인_티바);
        shopReviewFixture.리뷰_5점(성빈_학생, 영업중이_아닌_신전_떡볶이);
        shopReviewFixture.리뷰_5점(성빈_학생, 김밥천국);
    }

    @Test
    void 혜택_카테고리를_조회한다() throws Exception {
        mockMvc.perform(
                get("/benefit/categories")
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
    void 특정_혜택을_제공하는_모든_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/benefit/{id}/shops", 배달비_무료.getId())
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                      "count": 4,
                      "shops": [
                        {
                          "category_ids": [],
                          "delivery": true,
                          "id": %d,
                          "name": "김밥천국",
                          "open": [
                            {
                              "day_of_week": "MONDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "21:00"
                            },
                            {
                              "day_of_week": "FRIDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "00:00"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "010-7574-1212",
                          "is_event": false,
                          "is_open": true,
                          "average_rate": 5.0,
                          "review_count": 1,
                          "benefit_detail": "무료"
                        },
                        {
                          "category_ids": [],
                          "delivery": true,
                          "id": %d,
                          "name": "마슬랜 치킨",
                          "open": [
                            {
                              "day_of_week": "MONDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "21:00"
                            },
                            {
                              "day_of_week": "FRIDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "00:00"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "010-7574-1212",
                          "is_event": false,
                          "is_open": true,
                          "average_rate": 4.0,
                          "review_count": 1
                        },
                        {
                          "category_ids": [],
                          "delivery": true,
                          "id": %d,
                          "name": "티바",
                          "open": [
                            {
                              "day_of_week": "SUNDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "00:00"
                            },
                            {
                              "day_of_week": "MONDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "TUESDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "WEDNESDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "THURSDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "FRIDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "SATURDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "010-7788-9900",
                          "is_event": false,
                          "is_open": true,
                          "average_rate": 4.0,
                          "review_count": 1
                        },
                        {
                          "category_ids": [],
                          "delivery": true,
                          "id": %d,
                          "name": "신전 떡볶이",
                          "open": [
                            {
                              "day_of_week": "SUNDAY",
                              "closed": false,
                              "open_time": "00:00",
                              "close_time": "00:00"
                            },
                            {
                              "day_of_week": "MONDAY",
                              "closed": false,
                              "open_time": "12:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "TUESDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "WEDNESDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "THURSDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "FRIDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            },
                            {
                              "day_of_week": "SATURDAY",
                              "closed": false,
                              "open_time": "11:30",
                              "close_time": "21:30"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "010-7788-9900",
                          "is_event": false,
                          "is_open": false,
                          "average_rate": 5.0,
                          "review_count": 1
                        }
                      ]
                    }
                """,
                김밥천국.getId(),
                마슬랜.getId(),
                영업중인_티바.getId(),
                영업중이_아닌_신전_떡볶이.getId())));
    }
}
