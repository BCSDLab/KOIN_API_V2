package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewReportAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewReportCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.review.ShopReviewReportCategoryRepository;
import in.koreatech.koin.domain.shop.repository.review.ShopReviewReportRepository;
import in.koreatech.koin.domain.shop.repository.review.ShopReviewRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.infrastructure.slack.eventlistener.ReviewEventListener;

class ShopReviewApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private ShopReviewAcceptanceFixture shopReviewFixture;

    @Autowired
    private ShopReviewReportAcceptanceFixture shopReviewReportFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private ShopReviewReportCategoryAcceptanceFixture shopReviewReportCategoryFixture;

    @MockBean
    private ReviewEventListener reviewEventListener;

    private ShopReview 준호_학생_리뷰;
    private ShopReview 익명_학생_리뷰;
    private Shop 신전_떡볶이;
    private Shop 티바;
    private Owner 현수_사장님;
    private Student 준호_학생;
    private Student 익명_학생;
    private String token_준호;
    private Department 컴퓨터_공학부;
    private ShopReviewReportCategory 신고_카테고리_1;
    private ShopReviewReportCategory 신고_카테고리_2;
    private ShopReviewReportCategory 신고_카테고리_3;
    private ShopReviewReportCategory 신고_카테고리_4;

    @Autowired
    private ShopReviewRepository shopReviewRepository;

    @Autowired
    private ShopReviewReportRepository shopReviewReportRepository;

    @Autowired
    private ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;

    private final int INITIAL_REVIEW_COUNT = 2;

    @BeforeAll
    void setUp() {
        clear();
        컴퓨터_공학부 = departmentFixture.컴퓨터공학부();
        준호_학생 = userFixture.준호_학생(컴퓨터_공학부, null);
        익명_학생 = userFixture.익명_학생(컴퓨터_공학부);
        현수_사장님 = userFixture.현수_사장님();
        신전_떡볶이 = shopFixture.영업중이_아닌_신전_떡볶이(현수_사장님);
        티바 = shopFixture._24시간_영업중인_티바(현수_사장님);
        token_준호 = userFixture.getToken(준호_학생.getUser());
        준호_학생_리뷰 = shopReviewFixture.리뷰_4점(준호_학생, 신전_떡볶이);
        익명_학생_리뷰 = shopReviewFixture.리뷰_4점(익명_학생, 신전_떡볶이);
        신고_카테고리_1 = shopReviewReportCategoryFixture.리뷰_신고_주제에_맞지_않음();
        신고_카테고리_2 = shopReviewReportCategoryFixture.리뷰_신고_스팸();
        신고_카테고리_3 = shopReviewReportCategoryFixture.리뷰_신고_욕설();
        신고_카테고리_4 = shopReviewReportCategoryFixture.리뷰_신고_기타();
    }

    @Test
    @DisplayName("사용자가 리뷰를 등록할 수 있다.")
    void createReview() throws Exception {
        mockMvc.perform(post("/shops/{shopId}/reviews", 티바.getId())
                .header("Authorization", "Bearer " + token_준호)
                .content(String.format("""
                    {
                      "rating": 4,
                      "content": "정말 맛있어요~!",
                      "image_urls": [
                        "https://static.koreatech.in/example.png"
                      ],
                      "menu_names": [
                        "치킨",
                        "피자"
                      ]
                    }
                    """))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 리뷰를_등록할_때_메뉴명을_공백으로_입력하면_예외가_발생한다() throws Exception {
        mockMvc.perform(post("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .content(String.format("""
                    {
                      "rating": 4,
                      "content": "정말 맛있어요~!",
                      "image_urls": [
                        "https://static.koreatech.in/example.png"
                      ],
                      "menu_names": [
                        " "
                      ]
                    }
                    """))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_내용을_작성하지_않고_리뷰를_등록할_수_있다() throws Exception {
        mockMvc.perform(post("/shops/{shopId}/reviews", 티바.getId())
                .header("Authorization", "Bearer " + token_준호)
                .content(String.format("""
                    {
                      "rating": 4,
                      "image_urls": [
                        "https://static.koreatech.in/example.png"
                      ],
                      "menu_names": [
                        "치킨",
                        "피자"
                      ]
                    }
                    """))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("사용자가 본인의 리뷰를 수정할 수 있다.")
    void modifyReview() throws Exception {
        mockMvc.perform(put("/shops/{shopId}/reviews/{reviewId}", 신전_떡볶이.getId(), 준호_학생_리뷰.getId())
                .header("Authorization", "Bearer " + token_준호)
                .content(String.format("""
                    {
                      "rating": 3,
                      "content": "정말 맛있어요!",
                      "image_urls": [
                        "https://static.koreatech.in/example1.png"
                      ],
                      "menu_names": [
                        "피자"
                      ]
                    }
                    """))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        ShopReview shopReview = shopReviewRepository.getByIdAndIsDeleted(준호_학생_리뷰.getId());
        assertSoftly(
            softly -> {
                softly.assertThat(shopReview.getRating()).isEqualTo(3);
                softly.assertThat(shopReview.getContent()).isEqualTo("정말 맛있어요!");
                softly.assertThat(shopReview.getImages().get(0).getImageUrls())
                    .isEqualTo("https://static.koreatech.in/example1.png");
                softly.assertThat(shopReview.getMenus().size()).isEqualTo(1);
            }
        );
    }

    @Test
    @DisplayName("로그인한 사용자가 리뷰를 조회할 수 있다.")
    void getReview() throws Exception {
        mockMvc.perform(get("/shops/{shopId}/reviews", 준호_학생_리뷰.getShop().getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 2,
                       "current_count": 2,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.0,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 2,
                           "5": 0
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 신고된_리뷰는_is_reported_가_true_이다() throws Exception {
        ShopReviewReport shopReviewReport = shopReviewReportFixture.리뷰_신고(준호_학생, 익명_학생_리뷰, UNHANDLED);
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 2,
                       "current_count": 2,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.0,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 2,
                           "5": 0
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": true,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    @DisplayName("비회원이 리뷰를 조회할 수 있다.")
    void getReviewByUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 2,
                       "current_count": 2,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.0,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 2,
                           "5": 0
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    @DisplayName("리뷰 신고 카테고리를 조회할 수 있다.")
    void getReviewReportCategories() throws Exception {
        mockMvc.perform(get("/shops/reviews/reports/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                      "count": 4,
                      "categories": [
                        {
                          "name": "%s",
                          "detail": "%s"
                        },
                        {
                          "name": "%s",
                          "detail": "%s"
                        },
                        {
                          "name": "%s",
                          "detail": "%s"
                        },
                        {
                          "name": "%s",
                          "detail": "%s"
                        }
                      ]
                    }
                    """,
                신고_카테고리_1.getName(),
                신고_카테고리_1.getDetail(),
                신고_카테고리_2.getName(),
                신고_카테고리_2.getDetail(),
                신고_카테고리_3.getName(),
                신고_카테고리_3.getDetail(),
                신고_카테고리_4.getName(),
                신고_카테고리_4.getDetail())
            ));
    }

    @Test
    @DisplayName("특정 리뷰를 신고한다.")
    void reportReview() throws Exception {
        mockMvc.perform(post("/shops/{shopId}/reviews/{reviewId}/reports", 준호_학생_리뷰.getShop().getId(), 준호_학생_리뷰.getId())
                .header("Authorization", "Bearer " + token_준호)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reports": [
                        {
                          "title": "기타",
                          "content": "적절치 못한 리뷰인 것 같습니다."
                        },
                        {
                          "title": "스팸",
                          "content": "광고가 포함된 리뷰입니다."
                        }
                      ]
                    }
                    """)
            )
            .andExpect(status().isNoContent());

        assertSoftly(
            softly -> {
                Optional<ShopReviewReport> shopReviewReport1 = shopReviewReportRepository.findById(1);
                Optional<ShopReviewReport> shopReviewReport2 = shopReviewReportRepository.findById(2);
                softly.assertThat(shopReviewReport1.isPresent()).isTrue();
                softly.assertThat(shopReviewReport1.get().getTitle()).isEqualTo("기타");
                softly.assertThat(shopReviewReport1.get().getContent()).isEqualTo("적절치 못한 리뷰인 것 같습니다.");
                softly.assertThat(shopReviewReport1.get().getReportStatus()).isEqualTo(UNHANDLED);
                softly.assertThat(shopReviewReport2.get().getTitle()).isEqualTo("스팸");
                softly.assertThat(shopReviewReport2.get().getContent()).isEqualTo("광고가 포함된 리뷰입니다.");
                softly.assertThat(shopReviewReport2.get().getReportStatus()).isEqualTo(UNHANDLED);
                forceVerify(() -> verify(reviewEventListener).onReviewReportRegister(any()));
                clear();
                setUp();
            }
        );
    }

    @Test
    @DisplayName("학생이 자신이 작성한 리뷰를 삭제한다.")
    void deleteMyReview() throws Exception {
        mockMvc.perform(delete("/shops/{shopId}/reviews/{reviewId}", 준호_학생_리뷰.getShop().getId(), 준호_학생_리뷰.getId())
                .header("Authorization", "Bearer " + token_준호)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        Optional<ShopReview> shopReview = shopReviewRepository.findById(1);
        assertSoftly(
            softly -> {
                softly.assertThat(shopReview.get().isDeleted()).isTrue();
            }
        );
    }

    @Test
    void 신고가_반려된_리뷰는_is_reported_가_false_이다() throws Exception {
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    """)
            )
            .andExpect(status().isOk());

    }

    @Test
    void 단일_리뷰를_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/shops/{shopId}/reviews/{reviewId}", 신전_떡볶이.getId(), 준호_학생_리뷰.getId())
                .header("Authorization", "Bearer " + token_준호)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 최신순으로_정렬하여_리뷰를_조회한다() throws Exception {
        ShopReview 최신_리뷰_2024_08_07 = shopReviewFixture.최신_리뷰_2024_08_07(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .queryParam("sorter", "LATEST")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 3,
                       "current_count": 3,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.0,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 3,
                           "5": 0
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-08-07"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                최신_리뷰_2024_08_07.getId(),
                최신_리뷰_2024_08_07.getRating(),
                최신_리뷰_2024_08_07.getReviewer().getUser().getNickname(),
                최신_리뷰_2024_08_07.getContent(),
                최신_리뷰_2024_08_07.getImages().get(0).getImageUrls(),
                최신_리뷰_2024_08_07.getMenus().get(0).getMenuName(),
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 오래된_순으로_정렬하여_리뷰를_조회한다() throws Exception {
        ShopReview 최신_리뷰_2024_08_07 = shopReviewFixture.최신_리뷰_2024_08_07(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .queryParam("sorter", "OLDEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 3,
                       "current_count": 3,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.0,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 3,
                           "5": 0
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-08-07"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName(),
                최신_리뷰_2024_08_07.getId(),
                최신_리뷰_2024_08_07.getRating(),
                최신_리뷰_2024_08_07.getReviewer().getUser().getNickname(),
                최신_리뷰_2024_08_07.getContent(),
                최신_리뷰_2024_08_07.getImages().get(0).getImageUrls(),
                최신_리뷰_2024_08_07.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 별점이_높은_순으로_정렬하여_리뷰를_조회한다() throws Exception {
        ShopReview 리뷰_5점 = shopReviewFixture.리뷰_5점(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .queryParam("sorter", "HIGHEST_RATING")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 3,
                       "current_count": 3,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.3,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 2,
                           "5": 1
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                리뷰_5점.getId(),
                리뷰_5점.getRating(),
                리뷰_5점.getReviewer().getUser().getNickname(),
                리뷰_5점.getContent(),
                리뷰_5점.getImages().get(0).getImageUrls(),
                리뷰_5점.getMenus().get(0).getMenuName(),
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 별점이_낮은_순으로_정렬하여_리뷰를_조회한다() throws Exception {
        ShopReview 리뷰_5점 = shopReviewFixture.리뷰_5점(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .queryParam("limit", "10")
                .queryParam("page", "1")
                .queryParam("sorter", "LOWEST_RATING")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "total_count": 3,
                       "current_count": 3,
                       "total_page": 1,
                       "current_page": 1,
                       "statistics": {
                         "average_rating": 4.3,
                         "ratings": {
                           "1": 0,
                           "2": 0,
                           "3": 0,
                           "4": 2,
                           "5": 1
                         }
                       },
                       "reviews": [
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": false,
                           "is_modified": false,
                           "is_reported": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getUser().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName(),
                리뷰_5점.getId(),
                리뷰_5점.getRating(),
                리뷰_5점.getReviewer().getUser().getNickname(),
                리뷰_5점.getContent(),
                리뷰_5점.getImages().get(0).getImageUrls(),
                리뷰_5점.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 자신의_리뷰를_최신순으로_조회한다() throws Exception {
        ShopReview 최신_리뷰_2024_08_07 = shopReviewFixture.최신_리뷰_2024_08_07(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews/me", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("sorter", "LATEST")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "count": 2,
                       "reviews": [
                       {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-08-07"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                최신_리뷰_2024_08_07.getId(),
                최신_리뷰_2024_08_07.getRating(),
                최신_리뷰_2024_08_07.getReviewer().getUser().getNickname(),
                최신_리뷰_2024_08_07.getContent(),
                최신_리뷰_2024_08_07.getImages().get(0).getImageUrls(),
                최신_리뷰_2024_08_07.getMenus().get(0).getMenuName(),
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 자신의_리뷰를_오래된_순으로_조회한다() throws Exception {
        ShopReview 최신_리뷰_2024_08_07 = shopReviewFixture.최신_리뷰_2024_08_07(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews/me", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("sorter", "OLDEST")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "count": 2,
                       "reviews": [
                       {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-08-07"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                최신_리뷰_2024_08_07.getId(),
                최신_리뷰_2024_08_07.getRating(),
                최신_리뷰_2024_08_07.getReviewer().getUser().getNickname(),
                최신_리뷰_2024_08_07.getContent(),
                최신_리뷰_2024_08_07.getImages().get(0).getImageUrls(),
                최신_리뷰_2024_08_07.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 자신의_리뷰를_별점이_높은_순으로_조회한다() throws Exception {
        ShopReview 리뷰_5점 = shopReviewFixture.리뷰_5점(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews/me", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("sorter", "HIGHEST_RATING")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "count": 2,
                       "reviews": [
                       {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                리뷰_5점.getId(),
                리뷰_5점.getRating(),
                리뷰_5점.getReviewer().getUser().getNickname(),
                리뷰_5점.getContent(),
                리뷰_5점.getImages().get(0).getImageUrls(),
                리뷰_5점.getMenus().get(0).getMenuName(),
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 자신의_리뷰를_별점이_낮은_순으로_조회한다() throws Exception {
        ShopReview 리뷰_5점 = shopReviewFixture.리뷰_5점(준호_학생, 신전_떡볶이);
        mockMvc.perform(get("/shops/{shopId}/reviews/me", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .queryParam("sorter", "LOWEST_RATING")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                       "count": 2,
                       "reviews": [
                       {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         },
                         {
                           "review_id": %d,
                           "rating": %d,
                           "nick_name": "%s",
                           "content": "%s",
                           "image_urls": [
                             "%s"
                           ],
                           "menu_names": [
                             "%s"
                           ],
                           "is_mine": true,
                           "is_modified": false,
                           "created_at": "2024-01-15"
                         }
                       ]
                     }
                    """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                리뷰_5점.getId(),
                리뷰_5점.getRating(),
                리뷰_5점.getReviewer().getUser().getNickname(),
                리뷰_5점.getContent(),
                리뷰_5점.getImages().get(0).getImageUrls(),
                리뷰_5점.getMenus().get(0).getMenuName())
            ));
    }

    @Test
    void 한_상점에_하루에_한번의_리뷰만_등록할_수_있다() throws Exception {
        // 두 번째 요청
        mockMvc.perform(post("/shops/{shopId}/reviews", 신전_떡볶이.getId())
                .header("Authorization", "Bearer " + token_준호)
                .content("""
                    {
                      "rating": 4,
                      "content": "정말 맛있어요~!",
                      "image_urls": ["https://static.koreatech.in/example.png"],
                      "menu_names": ["치킨", "피자"]
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
