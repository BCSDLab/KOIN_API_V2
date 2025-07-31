package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ActivityAcceptanceFixture;

class ActivityApiTest extends AcceptanceTest {

    @Autowired
    protected ActivityAcceptanceFixture activityFixture;

    @Test
    void BCSDLab_활동_내역을_조회한다() throws Exception {
        activityFixture.builder()
            .title("BCSD/KAP 통합")
            .description("BCSD와 KAP가 통합되었습니다.")
            .imageUrls("https://test.com.png")
            .date(LocalDate.of(2018, 9, 12))
            .isDeleted(false)
            .build();

        activityFixture.builder()
            .title("19-3기 모집")
            .description("BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.")
            .imageUrls("""
                https://test2.com.png,
                https://test3.com.png
                """)
            .date(LocalDate.of(2019, 7, 29))
            .isDeleted(false)
            .build();

        activityFixture.builder()
            .title("코인 시간표 기능 추가")
            .description("더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다")
            .imageUrls("https://test4.com.png")
            .date(LocalDate.of(2019, 8, 20))
            .isDeleted(false)
            .build();

        mockMvc.perform(
                get("/activities")
                    .param("year", "2019")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "Activities": [
                        {
                            "date": "2019-07-29",
                            "is_deleted": false,
                            "updated_at": "2024-01-15 12:00:00",
                            "created_at": "2024-01-15 12:00:00",
                            "description": "BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.",
                            "image_urls": [
                                "https://test2.com.png",
                                "https://test3.com.png"
                            ],
                            "id": 2,
                            "title": "19-3기 모집"
                        },
                        {
                            "date": "2019-08-20",
                            "is_deleted": false,
                            "updated_at": "2024-01-15 12:00:00",
                            "created_at": "2024-01-15 12:00:00",
                            "description": "더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다",
                            "image_urls": [
                                "https://test4.com.png"
                            ],
                            "id": 3,
                            "title": "코인 시간표 기능 추가"
                        }
                    ]
                }
                """));
    }

    @Test
    void BCSDLab_활동_내역을_조회한다_파라미터가_없는_경우_전체조회() throws Exception {
        activityFixture.builder()
            .title("BCSD/KAP 통합")
            .description("BCSD와 KAP가 통합되었습니다.")
            .imageUrls("https://test.com.png")
            .date(LocalDate.of(2018, 9, 12))
            .isDeleted(false)
            .build();

        activityFixture.builder()
            .title("19-3기 모집")
            .description("BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.")
            .imageUrls("""
                https://test2.com.png,
                https://test3.com.png
                """)
            .date(LocalDate.of(2019, 7, 29))
            .isDeleted(false)
            .build();

        activityFixture.builder()
            .title("코인 시간표 기능 추가")
            .description("더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다")
            .imageUrls("https://test4.com.png")
            .date(LocalDate.of(2019, 8, 20))
            .isDeleted(false)
            .build();

        mockMvc.perform(
                get("/activities")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "Activities": [
                        {
                            "date": "2018-09-12",
                            "is_deleted": false,
                            "updated_at": "2024-01-15 12:00:00",
                            "created_at": "2024-01-15 12:00:00",
                            "description": "BCSD와 KAP가 통합되었습니다.",
                            "image_urls": [
                                "https://test.com.png"
                            ],
                            "id": 1,
                            "title": "BCSD/KAP 통합"
                        },
                        {
                            "date": "2019-07-29",
                            "is_deleted": false,
                            "updated_at": "2024-01-15 12:00:00",
                            "created_at": "2024-01-15 12:00:00",
                            "description": "BCSD Lab과 함께 성장해나갈 인재를 모집했습니다.",
                            "image_urls": [
                                "https://test2.com.png",
                                "https://test3.com.png"
                            ],
                            "id": 2,
                            "title": "19-3기 모집"
                        },
                        {
                            "date": "2019-08-20",
                            "is_deleted": false,
                            "updated_at": "2024-01-15 12:00:00",
                            "created_at": "2024-01-15 12:00:00",
                            "description": "더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다",
                            "image_urls": [
                                "https://test4.com.png"
                            ],
                            "id": 3,
                            "title": "코인 시간표 기능 추가"
                        }
                    ]
                }
                """));
    }
}
