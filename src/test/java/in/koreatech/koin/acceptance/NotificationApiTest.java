package in.koreatech.koin.acceptance;

import static in.koreatech.koin.integration.fcm.notification.model.NotificationDetailSubscribeType.LUNCH;
import static in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribeType.SHOP_EVENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.DepartmentFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.integration.fcm.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribe;
import in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribeType;
import in.koreatech.koin.integration.fcm.notification.repository.NotificationSubscribeRepository;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationApiTest extends AcceptanceTest {

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentFixture departmentFixture;

    User user;
    String userToken;
    String deviceToken;
    Department department;

    @BeforeAll
    void setUp() {
        clear();
        department = departmentFixture.컴퓨터공학부();
        user = userFixture.준호_학생(department, null).getUser();
        userToken = userFixture.getToken(user);
        deviceToken = "testToken";
    }

    @Test
    void 알림_구독_내역을_조회한다() throws Exception {
        //given
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(notificationSubscribe);

        MvcResult result = mockMvc.perform(
                get("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "is_permit": false,
                    "subscribes": [
                        {
                            "type": "SHOP_EVENT",
                            "is_permit": true,
                            "detail_subscribes": [
                               \s
                            ]
                        },
                        {
                            "type": "DINING_SOLD_OUT",
                            "is_permit": false,
                            "detail_subscribes": [
                                {
                                    "detail_type": "BREAKFAST",
                                    "is_permit": false
                                },
                                {
                                    "detail_type": "LUNCH",
                                    "is_permit": false
                                },
                                {
                                    "detail_type": "DINNER",
                                    "is_permit": false
                                }
                            ]
                        },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "ARTICLE_KEYWORD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "REVIEW_PROMPT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "LOST_ITEM_CHAT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "MARKETING",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                    ]
                }
            """))
            .andReturn();
    }

    @Test
    void 전체_알림을_구독한다_디바이스_토큰을_추가한다() throws Exception {
        //when then
        MvcResult result = mockMvc.perform(
                post("/notification")
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andReturn();

        User resultUser = userRepository.getById(user.getId());
        assertThat(resultUser.getDeviceToken()).isEqualTo(deviceToken);
    }

    @Test
    void 특정_알림을_구독한다() throws Exception {
        String notificationType = SHOP_EVENT.name();

        mockMvc.perform(
                post("/notification")
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/notification/subscribe")
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .queryParam("type", notificationType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                get("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": true,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": false,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "ARTICLE_KEYWORD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "REVIEW_PROMPT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "LOST_ITEM_CHAT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "MARKETING",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """));
    }

    @Test
    void 특정_세부알림을_구독한다() throws Exception {
        String notificationType = DINING_SOLD_OUT.name();
        String notificationDetailType = LUNCH.name();

        mockMvc.perform(
                post("/notification")
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/notification/subscribe")
                    .queryParam("type", notificationType)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/notification/subscribe/detail")
                    .queryParam("detail_type", notificationDetailType)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                get("/notification")
                    .queryParam("detail_type", notificationDetailType)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""

                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": true
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "ARTICLE_KEYWORD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "REVIEW_PROMPT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "LOST_ITEM_CHAT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "MARKETING",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """));
    }

    @Test
    void 전체_알림_구독을_취소한다_디바이스_토큰을_삭제한다() throws Exception {
        user.permitNotification(deviceToken);

        mockMvc.perform(
                delete("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        User result = userRepository.getById(user.getId());
        assertThat(result.getDeviceToken()).isNull();
    }

    @Test
    void 특정_알림_구독을_취소한다() throws Exception {
        var SubscribeShopEvent = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .user(user)
            .build();

        var SubscribeDiningSoldOut = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(SubscribeShopEvent);
        notificationSubscribeRepository.save(SubscribeDiningSoldOut);

        String notificationType = SHOP_EVENT.name();

        mockMvc.perform(
                post("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                delete("/notification/subscribe")
                    .header("Authorization", "Bearer " + userToken)
                    .queryParam("type", notificationType)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""

                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "ARTICLE_KEYWORD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "REVIEW_PROMPT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "LOST_ITEM_CHAT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "MARKETING",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """));
        ;
    }

    @Test
    void 특정_세부_알림_구독을_취소한다() throws Exception {
        var SubscribeDiningSoldOut = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .user(user)
            .build();

        var SubscribeBreakfast = NotificationSubscribe.builder()
            .detailType(NotificationDetailSubscribeType.BREAKFAST)
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .user(user)
            .build();

        var SubscribeLunch = NotificationSubscribe.builder()
            .detailType(NotificationDetailSubscribeType.LUNCH)
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(SubscribeDiningSoldOut);
        notificationSubscribeRepository.save(SubscribeBreakfast);
        notificationSubscribeRepository.save(SubscribeLunch);

        String notificationType = DINING_SOLD_OUT.name();
        String notificationDetailType = LUNCH.name();

        mockMvc.perform(
                post("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .content(String.format("""
                    {
                      "device_token": "%s"
                    }
                    """, deviceToken))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/notification/subscribe")
                    .header("Authorization", "Bearer " + userToken)
                    .queryParam("type", notificationType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                delete("/notification/subscribe/detail")
                    .header("Authorization", "Bearer " + userToken)
                    .queryParam("detail_type", notificationDetailType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/notification")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": true
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "ARTICLE_KEYWORD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "REVIEW_PROMPT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "LOST_ITEM_CHAT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "MARKETING",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """));
    }
}
