package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.infrastructure.slack.eventlistener.OwnerEventListener;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OwnerShopRedisRepository ownerShopRedisRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OwnerEventListener ownerEventListener;

    @Test
    void 사장님이_로그인을_진행한다() throws Exception {
        Owner owner = userFixture.원경_사장님();
        String phoneNumber = owner.getAccount();
        String password = "1234";

        mockMvc.perform(
                post("/owner/login")
                    .content("""
                        {
                          "account" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(phoneNumber, password))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", userFixture.맥북userAgent헤더())
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_승인하지_않은_사장님이_로그인을_진행한다() throws Exception {
        Owner owner = userFixture.철수_사장님();
        String phoneNumber = owner.getAccount();
        String password = "1234";

        mockMvc.perform(
                post("/owner/login")
                    .content("""
                        {
                          "account" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(phoneNumber, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 로그인된_사장님_정보를_조회한다() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner, null);
        String token = userFixture.getToken(owner.getUser());

        mockMvc.perform(
                get("/owner")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "email": null,
                      "name": "테스트용_현수",
                      "company_number": "123-45-67190",
                      "account" : "01098765432",
                      "attachments": [
                          {
                              "id": 1,
                              "file_url": "https://test.com/현수_사장님_인증사진_1.jpg",
                              "file_name": "현수_사장님_인증사진_1.jpg"
                          },
                          {
                              "id": 2,
                              "file_url": "https://test.com/현수_사장님_인증사진_2.jpg",
                              "file_name": "현수_사장님_인증사진_2.jpg"
                          }
                      ],
                      "shops": [
                          {
                              "id": 1,
                              "name": "마슬랜 치킨"
                          }
                      ]
                  }
                """));
    }

    @Nested
    @DisplayName("사장님 회원가입")
    @Transactional
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ownerRegister {

        @Test
        void 사장님이_전화번호를_아이디로_회원가입_요청을_한다() throws Exception {
            // when & then
            mockMvc.perform(
                    post("/owners/register/phone")
                        .content("""
                                {
                                   "attachment_urls": [
                                     {
                                       "file_url": "https://static.koreatech.in/testimage.png"
                                     }
                                   ],
                                   "company_number": "012-34-56789",
                                   "name": "최준호",
                                   "password": "a0240120305812krlakdsflsa;1235",
                                   "phone_number": "01012341234",
                                   "shop_number": "0510000000",
                                   "shop_name": "기분좋은 뷔짱"
                                }
                            """)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

            // when
            transactionTemplate.executeWithoutResult(status -> {
                    Owner owner = ownerRepository.findByCompanyRegistrationNumber("012-34-56789").get();
                    assertSoftly(
                        softly -> {
                            softly.assertThat(owner).isNotNull();
                            softly.assertThat(owner.getUser().getName()).isEqualTo("최준호");
                            softly.assertThat(owner.getUser().getEmail()).isEqualTo(null);
                            softly.assertThat(owner.getUser().getPhoneNumber()).isEqualTo("01012341234");
                            softly.assertThat(owner.getCompanyRegistrationNumber()).isEqualTo("012-34-56789");
                            softly.assertThat(owner.getAccount()).isEqualTo("01012341234");
                            softly.assertThat(owner.getAttachments().size()).isEqualTo(1);
                            softly.assertThat(owner.getAttachments().get(0).getUrl())
                                .isEqualTo("https://static.koreatech.in/testimage.png");
                            softly.assertThat(owner.getUser().isAuthed()).isFalse();
                            softly.assertThat(owner.getUser().isDeleted()).isFalse();
                            forceVerify(() -> verify(ownerEventListener).onOwnerRegisterBySms(any()));
                            clear();
                        }
                    );
                }
            );
        }

        @Test
        void 사장님이_전화번호를_아이디로_회원가입을_했을_때_저장된_가게정보를_조회한다() throws Exception {
            Owner owner = userFixture.현수_사장님();
            String token = userFixture.getToken(owner.getUser());

            OwnerShop ownerShop = OwnerShop.builder()
                .ownerId(owner.getId())
                .shopName("가게명")
                .shopNumber("01012345678")
                .build();

            ownerShopRedisRepository.save(ownerShop);

            mockMvc.perform(
                    get("/owner/registered-store")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "shop_name": "가게명",
                        "shop_number": "01012345678"
                    }
                    """));
        }
    }

    @Test
    void 사장님이_회원탈퇴를_한다() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        String token = userFixture.getToken(owner.getUser());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/user")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
        // then
        assertThat(userRepository.findById(owner.getId())).isNotPresent();
    }

    @Test
    void 사업자_등록번호_중복_검증_존재하지_않으면_200() throws Exception {
        // when & then
        mockMvc.perform(
                post("/owners/exists/company-number")
                    .content("""
                                {
                                   "company_number": "123-45-67190"
                                }
                            """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사업자_등록번호_중복_검증_이미_존재하면_409() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        // when & then
        mockMvc.perform(
                post("/owners/exists/company-number")
                    .content("""
                                {
                                   "company_number": "%s"
                                }
                            """.formatted(owner.getCompanyRegistrationNumber()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 사업자_등록번호_중복_검증_값이_존재하지_않으면_400() throws Exception {
        // when & then
        mockMvc.perform(
                post("/owners/exists/company-number")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사업자_등록번호_중복_검증_값이_올바르지_않으면_400() throws Exception {
        // when & then
        mockMvc.perform(
                post("/owners/exists/company-number")
                    .content("""
                                {
                                   "company_number": "1234567890"
                                }
                            """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_존재하지_않으면_200() throws Exception {
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("account", "01012345678")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_이미_존재하면_409() throws Exception {
        Owner owner = userFixture.현수_사장님();
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("account", owner.getAccount())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_파라미터에_전화번호를_포함하지_않으면_400() throws Exception {
        mockMvc.perform(
                get("/owners/exists/account")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_잘못된_전화번호_형식이면_400() throws Exception {
        String phoneNumber = "123123123123";
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("phone_number", phoneNumber)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }
}
