package in.koreatech.koin.acceptance;

import java.time.LocalDate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.domain.activity.repository.ActivityRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class ActivityApiTest extends AcceptanceTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Test
    @DisplayName("BCSD Lab 활동 내역을 조회한다.")
    void getActivities() {
        Activity request1 = Activity.builder()
            .title("BCSD/KAP 통합")
            .description("BCSD와 KAP가 통합되었습니다.")
            .imageUrls(
                "https://static.koreatech.in/upload/a26brbr.png")
            .date(LocalDate.of(2018, 9, 12))
            .isDeleted(false)
            .build();

        Activity request2 = Activity.builder()
            .title("19-3기 모집")
            .description("BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.")
            .imageUrls("""
                https://static.koreatech.in/upload/a1tt.png,
                https://static.koreatech.in/upload/a4bc.png
                """)
            .date(LocalDate.of(2019, 7, 29))
            .isDeleted(false)
            .build();

        Activity request3 = Activity.builder()
            .title("코인 시간표 기능 추가")
            .description("더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다")
            .imageUrls(
                "https://static.koreatech.in/upload/a2tt.png"
            )
            .date(LocalDate.of(2019, 8, 20))
            .isDeleted(false)
            .build();

        Activity activity1 = activityRepository.save(request1);
        Activity activity2 = activityRepository.save(request2);
        Activity activity3 = activityRepository.save(request3);

        LocalDate expectedDate1 = request1.getDate();
        LocalDate expectedDate2 = request2.getDate();
        LocalDate expectedDate3 = request3.getDate();

        ExtractableResponse<Response> response = RestAssured
            .given().log().all()
            .when()
            .get("/activities?year=2019")
            .then().log().all()
            .statusCode(HttpStatus.OK.value()).log().all()
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                // activity2
                softly.assertThat(response.body().jsonPath().getList("Activities").size()).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getLong("Activities[0].id")).isEqualTo(activity2.getId());
                softly.assertThat(response.body().jsonPath().getString("Activities[0].title"))
                    .isEqualTo(activity2.getTitle());
                softly.assertThat(response.body().jsonPath().getString("Activities[0].description"))
                    .isEqualTo(activity2.getDescription());
                softly.assertThat(response.body().jsonPath().getList("Activities[0].image_urls")).hasSize(2);
                LocalDate actualDate = LocalDate.parse(response.body().jsonPath().getString("Activities[0].date"));
                softly.assertThat(actualDate).isEqualTo(expectedDate2);
                softly.assertThat(response.body().jsonPath().getBoolean("Activities[0].is_deleted"))
                    .isEqualTo(activity2.getIsDeleted());

                //activity3
                softly.assertThat(response.body().jsonPath().getLong("Activities[1].id")).isEqualTo(activity3.getId());
                softly.assertThat(response.body().jsonPath().getString("Activities[1].title"))
                    .isEqualTo(activity3.getTitle());
                softly.assertThat(response.body().jsonPath().getString("Activities[1].description"))
                    .isEqualTo(activity3.getDescription());
                softly.assertThat(response.body().jsonPath().getList("Activities[1].image_urls")).hasSize(1);
                LocalDate actualDate2 = LocalDate.parse(response.body().jsonPath().getString("Activities[1].date"));
                softly.assertThat(actualDate2).isEqualTo(expectedDate3);
                softly.assertThat(response.body().jsonPath().getBoolean("Activities[1].is_deleted"))
                    .isEqualTo(activity3.getIsDeleted());
            }
        );

    }

    @Test
    @DisplayName("Year 파라미터 없이 모든 BCSD Lab 활동 내역을 조회한다.")
    void getActivitiesWithoutYear() {
        Activity request1 = Activity.builder()
            .title("BCSD/KAP 통합")
            .description("BCSD와 KAP가 통합되었습니다.")
            .imageUrls(
                "https://static.koreatech.in/upload/a26brbr.png")
            .date(LocalDate.of(2018, 9, 12))
            .isDeleted(false)
            .build();

        Activity request2 = Activity.builder()
            .title("19-3기 모집")
            .description("BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.")
            .imageUrls("""
                https://static.koreatech.in/upload/a1tt.png,
                https://static.koreatech.in/upload/a4bc.png
                """)
            .date(LocalDate.of(2019, 7, 29))
            .isDeleted(false)
            .build();

        Activity request3 = Activity.builder()
            .title("코인 시간표 기능 추가")
            .description("더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다")
            .imageUrls(
                "https://static.koreatech.in"
            )
            .date(LocalDate.of(2019, 8, 20))
            .isDeleted(false)
            .build();

        Activity activity1 = activityRepository.save(request1);
        Activity activity2 = activityRepository.save(request2);
        Activity activity3 = activityRepository.save(request3);

        LocalDate expectedDate1 = request1.getDate(); // LocalDate 객체
        LocalDate expectedDate2 = request2.getDate(); // LocalDate 객체
        LocalDate expectedDate3 = request3.getDate(); // LocalDate 객체

        ExtractableResponse<Response> response = RestAssured
            .given().log().all()
            .when().log().all()
            .get("/activities")
            .then().log().all()
            .statusCode(HttpStatus.OK.value()).log().all()
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                // activity1
                softly.assertThat(response.body().jsonPath().getList("Activities").size()).isEqualTo(3);
                softly.assertThat(response.body().jsonPath().getLong("Activities[0].id")).isEqualTo(activity1.getId());
                softly.assertThat(response.body().jsonPath().getString("Activities[0].title"))
                    .isEqualTo(activity1.getTitle());
                softly.assertThat(response.body().jsonPath().getString("Activities[0].description"))
                    .isEqualTo(activity1.getDescription());
                softly.assertThat(response.body().jsonPath().getList("Activities[0].image_urls")).hasSize(1);
                LocalDate actualDate = LocalDate.parse(response.body().jsonPath().getString("Activities[0].date"));
                softly.assertThat(actualDate).isEqualTo(expectedDate1);
                softly.assertThat(response.body().jsonPath().getBoolean("Activities[0].is_deleted"))
                    .isEqualTo(activity1.getIsDeleted());

                //activity3
                softly.assertThat(response.body().jsonPath().getLong("Activities[2].id")).isEqualTo(activity3.getId());
                softly.assertThat(response.body().jsonPath().getString("Activities[2].title"))
                    .isEqualTo(activity3.getTitle());
                softly.assertThat(response.body().jsonPath().getString("Activities[2].description"))
                    .isEqualTo(activity3.getDescription());
                softly.assertThat(response.body().jsonPath().getList("Activities[2].image_urls")).hasSize(1);
                LocalDate actualDate2 = LocalDate.parse(response.body().jsonPath().getString("Activities[2].date"));
                softly.assertThat(actualDate2).isEqualTo(expectedDate3);
                softly.assertThat(response.body().jsonPath().getBoolean("Activities[2].is_deleted"))
                    .isEqualTo(activity2.getIsDeleted());
            }
        );

    }

}
