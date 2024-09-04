package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coop.model.DiningSoldOutCache;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.CoopShopFixture;
import in.koreatech.koin.fixture.DiningFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiningApiTest extends AcceptanceTest {

    @Autowired
    private DiningRepository diningRepository;

    @Autowired
    private DiningSoldOutCacheRepository diningSoldOutCacheRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private DiningFixture diningFixture;

    @Autowired
    private CoopShopFixture coopShopFixture;

    private Dining A코너_점심;
    private User coop_준기;
    private String token_준기;
    private User owner_현수;
    private String token_현수;
    private CoopShop 학생식당;

    @BeforeAll
    void setUp() {
        clearTable();
        coop_준기 = userFixture.준기_영양사().getUser();
        token_준기 = userFixture.getToken(coop_준기);
        owner_현수 = userFixture.현수_사장님().getUser();
        token_현수 = userFixture.getToken(owner_현수);
        A코너_점심 = diningFixture.A코너_점심(LocalDate.parse("2024-01-15"));
        학생식당 = coopShopFixture.학생식당();
    }

    @Test
    void 특정_날짜의_모든_식단들을_조회한다() throws Exception {
        mockMvc.perform(
                get("/dinings?date=240115")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코너",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null,
                        "likes": 0,
                        "is_liked" : false
                    }
                ]
                """));
    }

    @Test
    void 잘못된_형식의_날짜로_조회한다_날짜의_형식이_잘못되었다면_400() throws Exception {

        mockMvc.perform(
                get("/dinings?date=20240115")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 날짜_비어있다_오늘_날짜를_받아_조회한다() throws Exception {
        mockMvc.perform(
                get("/dinings")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코너",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null,
                        "likes": 0,
                        "is_liked" : false
                    }
                ]
                """));
    }

    @Test
    void 영양사_권한으로_품절_요청을_보낸다() throws Exception {
        mockMvc.perform(
                patch("/coop/dining/soldout")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_점심.getId(), true))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 권한이_없는_사용자가_품절_요청을_보낸다() throws Exception {
        mockMvc.perform(
                patch("/coop/dining/soldout")
                    .header("Authorization", "Bearer " + token_현수)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "sold_out": %s
                    }
                    """, A코너_점심.getId(), true))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 영양사님_권한으로_식단_이미지를_업로드한다_이미지_URL이_DB에_저장된다() throws Exception {
        String imageUrl = "https://stage.koreatech.in/image.jpg";
        mockMvc.perform(
                patch("/coop/dining/image")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "image_url": "%s"
                    }
                    """, A코너_점심.getId(), imageUrl))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();
        Dining dining = diningRepository.getById(A코너_점심.getId());
        assertThat(dining.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    void 허용되지_않은_권한으로_식단_이미지를_업로드한다_권한_오류() throws Exception {
        String imageUrl = "https://stage.koreatech.in/image.jpg";
        mockMvc.perform(
                patch("/coop/dining/image")
                    .header("Authorization", "Bearer " + token_현수)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "image_url": "%s"
                    }
                    """, A코너_점심.getId(), imageUrl))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    void 해당_식사시간에_품절_요청을_한다_품절_알림이_발송된다() throws Exception {
        mockMvc.perform(
                patch("/coop/dining/soldout")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "sold_out": "%s"
                    }
                    """, A코너_점심.getId(), true))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        forceVerify(() -> verify(coopEventListener).onDiningSoldOutRequest(any()));
        clearTable();
        setUp();
    }

    @Test
    void 해당_식사시간_외에_품절_요청을_한다_품절_알림이_발송되지_않는다() throws Exception {
        Dining A코너_저녁 = diningFixture.A코너_저녁(LocalDate.parse("2024-01-15"));
        mockMvc.perform(
                patch("/coop/dining/soldout")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "sold_out": "%s"
                    }
                    """, A코너_저녁.getId(), true))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();
        forceVerify(() -> verify(coopEventListener, never()).onDiningSoldOutRequest(any()));
        clearTable();
        setUp();
    }

    @Test
    void 동일한_식단_코너의_두_번째_품절_요청은_알림이_가지_않는다() throws Exception {
        diningSoldOutCacheRepository.save(DiningSoldOutCache.from(A코너_점심.getPlace()));
        mockMvc.perform(
                patch("/coop/dining/soldout")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                    {
                        "menu_id": "%s",
                        "sold_out": "%s"
                    }
                    """, A코너_점심.getId(), true))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();
        forceVerify(() -> verify(coopEventListener, never()).onDiningSoldOutRequest(any()));
        clearTable();
        setUp();
    }

    @Test
    void 특정_식단의_좋아요를_누른다() throws Exception {
        mockMvc.perform(
                patch("/dining/like")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    void 특정_식단의_좋아요_중복해서_누르면_에러() throws Exception {
        mockMvc.perform(
                patch("/dining/like")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        mockMvc.perform(
                patch("/dining/like")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    void 좋아요_누른_식단은_isLiked가_true로_반환() throws Exception {
        mockMvc.perform(
                patch("/dining/like")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        mockMvc.perform(
                get("/dinings?date=240115")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코너",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null,
                        "likes": 1,
                        "is_liked" : true
                    }
                ]
                """))
            .andReturn();
    }

    @Test
    void 좋아요_안누른_식단은_isLiked가_false로_반환() throws Exception {
        mockMvc.perform(
                get("/dinings?date=240115")
                    .header("Authorization", "Bearer " + token_준기)
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코너",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null,
                        "likes": 0,
                        "is_liked" : false
                    }
                ]
                """))
            .andReturn();
    }

    @Test
    void 이미지_업로드를_한다_품절_알림이_발송된다() throws Exception {
        String imageUrl = "https://stage.koreatech.in/image.jpg";
        mockMvc.perform(
                patch("/coop/dining/image")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                        {
                            "menu_id": "%s",
                            "image_url": "%s"
                        }
                    """, A코너_점심.getId(), imageUrl))
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        forceVerify(() -> verify(coopEventListener).onDiningImageUploadRequest(any()));
        clearTable();
        setUp();
    }

    @Test
    void 해당_식사시간_외에_이미지_업로드를_한다_품절_알림이_발송되지_않는다() throws Exception {
        Dining A코너_저녁 = diningFixture.A코너_저녁(LocalDate.parse("2024-01-15"));
        String imageUrl = "https://stage.koreatech.in/image.jpg";
        mockMvc.perform(
                patch("/coop/dining/image")
                    .header("Authorization", "Bearer " + token_준기)
                    .content(String.format("""
                        {
                            "menu_id": "%s",
                            "image_url": "%s"
                        }
                    """, A코너_저녁.getId(), imageUrl))
                    .param("diningId", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        forceVerify(() -> verify(coopEventListener, never()).onDiningImageUploadRequest(any()));
        clearTable();
        setUp();
    }

    @Test
    void 특정_메뉴_특정_코너의_식단을_검색한다() throws Exception {
        mockMvc.perform(
                get("/dinings/search?keyword=육개장&page=1&limit=10&filter=A코너")
                    .header("Authorization", "Bearer " + token_준기)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "total_count": 1,
                     "current_count": 1,
                     "total_page": 1,
                     "current_page": 1,
                     "dinings": [
                         {
                             "id": 1,
                             "date": "2024-01-15",
                             "type": "LUNCH",
                             "place": "A코너",
                             "kcal": 881,
                             "menu": [
                                 "병아리콩밥",
                                 "(탕)소고기육개장",
                                 "땡초부추전",
                                 "누룽지탕"
                             ],
                             "image_url": null,
                             "created_at": "2024-01-15 12:00:00",
                             "soldout_at": "2024-01-15 12:00:00",
                             "changed_at": "2024-01-15 12:00:00",
                             "likes": 0
                         }
                     ]
                }
                """))
            .andReturn();
    }

    @Test
    void 특정_메뉴_특정_코너의_식단을_검색한다_해당사항_없을_경우() throws Exception {
        mockMvc.perform(
                get("/dinings/search?keyword=육개장&page=1&limit=10&filter=B코너")
                    .header("Authorization", "Bearer " + token_준기)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "total_count": 0,
                     "current_count": 0,
                     "total_page": 0,
                     "current_page": 1,
                     "dinings": []
                }
                """))
            .andReturn();
    }

    @Test
    void 특정_메뉴의_식단을_검색한다_필터_없을_경우() throws Exception {
        mockMvc.perform(
                get("/dinings/search?keyword=육개장&page=1&limit=10&filter=")
                    .header("Authorization", "Bearer " + token_준기)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "total_count": 1,
                     "current_count": 1,
                     "total_page": 1,
                     "current_page": 1,
                     "dinings": [
                         {
                             "id": 1,
                             "date": "2024-01-15",
                             "type": "LUNCH",
                             "place": "A코너",
                             "kcal": 881,
                             "menu": [
                                 "병아리콩밥",
                                 "(탕)소고기육개장",
                                 "땡초부추전",
                                 "누룽지탕"
                             ],
                             "image_url": null,
                             "created_at": "2024-01-15 12:00:00",
                             "soldout_at": "2024-01-15 12:00:00",
                             "changed_at": "2024-01-15 12:00:00",
                             "likes": 0
                         }
                     ]
                 }
                """))
            .andReturn();
    }
}
