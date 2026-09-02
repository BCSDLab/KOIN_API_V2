package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.CHAT_ROOM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_CHAT_MESSAGE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;

class TeamRecruitmentChatApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    @Autowired
    private TeamRecruitmentChatMessageRepository chatMessageRepository;

    @Autowired
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Autowired
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Student author;
    private Student applicant;
    private User outsider;
    private String authorToken;
    private String applicantToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        author = userFixture.준호_학생(department, null);
        applicant = userFixture.성빈_학생(department);
        outsider = userRepository.save(User.builder()
            .name("테스트용_외부")
            .nickname("외부조회자")
            .phoneNumber("01000000099")
            .email("outsider@koreatech.ac.kr")
            .loginId("outsider")
            .loginPw("1234")
            .userType(UserType.STUDENT)
            .isAuthed(true)
            .isDeleted(false)
            .build());
        authorToken = userFixture.getToken(author.getUser());
        applicantToken = userFixture.getToken(applicant.getUser());
        outsiderToken = userFixture.getToken(outsider);
    }

    @Test
    @DisplayName("TEAM 채팅방은 실제 HTTP에서 멤버만 조회할 수 있다")
    void teamChatRoomRequiresMembership() throws Exception {
        TeamRecruitment recruitment = saveRecruitment("TEAM 채팅 HTTP");
        TeamRecruitmentChatRoom room = saveTeamRoom(recruitment, ACTIVE);

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}", recruitment.getId(), room.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chat_room_id").value(room.getId()))
            .andExpect(jsonPath("$.room_name").value(recruitment.getTitle()))
            .andExpect(jsonPath("$.room_type").value("TEAM"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.member_count").value(2));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}", recruitment.getId(), room.getId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}", recruitment.getId(), room.getId())
                .header("Authorization", "Bearer " + outsiderToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_CHAT_FORBIDDEN"));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}", recruitment.getId() + 1, room.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_CHAT_NOT_FOUND"));
    }

    @Test
    @DisplayName("TEAM 채팅은 메시지 polling cursor와 unread read를 실제 HTTP로 반영하고 알림 outbox를 만든다")
    void teamChatMessagePollingAndNotificationOutbox() throws Exception {
        TeamRecruitment recruitment = saveRecruitment("메시지 polling");
        TeamRecruitmentChatRoom room = saveTeamRoom(recruitment, ACTIVE);
        entityManager.flush();

        MvcResult firstMessageResult = sendMessage(room, recruitment, authorToken, "첫 번째 메시지")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message_id").isNumber())
            .andExpect(jsonPath("$.user_nickname").value(author.getUser().getNickname()))
            .andExpect(jsonPath("$.content").value("첫 번째 메시지"))
            .andExpect(jsonPath("$.is_image").value(false))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andReturn();

        entityManager.flush();
        TeamRecruitmentChatMessage firstMessage = chatMessageRepository
            .findTopByChatRoom_IdOrderByIdDesc(room.getId())
            .orElseThrow();
        Integer firstMessageId = firstMessage.getId();
        assertThat(firstMessageResult.getResponse().getContentAsString()).contains("\"message_id\":" + firstMessageId);

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].message_id").value(firstMessageId))
            .andExpect(jsonPath("$[0].content").value("첫 번째 메시지"))
            .andExpect(jsonPath("$[0].unread_count").value(0));

        assertThat(chatMemberRepository.findByChatRoom_IdAndUser_Id(room.getId(), applicant.getUser().getId())
            .orElseThrow().getLastReadMessageId()).isEqualTo(firstMessageId);

        MvcResult secondMessageResult = sendMessage(room, recruitment, applicantToken, "두 번째 메시지")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("두 번째 메시지"))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andReturn();
        entityManager.flush();
        Integer secondMessageId = chatMessageRepository
            .findTopByChatRoom_IdOrderByIdDesc(room.getId())
            .orElseThrow().getId();
        assertThat(secondMessageResult.getResponse().getContentAsString()).contains("\"message_id\":" + secondMessageId);

        MvcResult thirdMessageResult = sendMessage(room, recruitment, authorToken, "세 번째 메시지")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("세 번째 메시지"))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andReturn();
        entityManager.flush();
        Integer thirdMessageId = chatMessageRepository
            .findTopByChatRoom_IdOrderByIdDesc(room.getId())
            .orElseThrow().getId();
        assertThat(thirdMessageResult.getResponse().getContentAsString()).contains("\"message_id\":" + thirdMessageId);

        MvcResult fourthMessageResult = sendMessage(room, recruitment, authorToken, "네 번째 메시지")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("네 번째 메시지"))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andReturn();
        entityManager.flush();
        Integer fourthMessageId = chatMessageRepository
            .findTopByChatRoom_IdOrderByIdDesc(room.getId())
            .orElseThrow().getId();
        assertThat(fourthMessageResult.getResponse().getContentAsString()).contains("\"message_id\":" + fourthMessageId);

        List<TeamRecruitmentNotification> applicantNotifications =
            notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(applicant.getUser().getId());
        assertThat(applicantNotifications)
            .hasSize(3)
            .allSatisfy(notification -> {
                assertThat(notification.getRecipient().getId()).isEqualTo(applicant.getUser().getId());
                assertThat(notification.getType()).isEqualTo(NEW_CHAT_MESSAGE);
                assertThat(notification.getTargetType()).isEqualTo(CHAT_ROOM);
                assertThat(notification.getRecruitment().getId()).isEqualTo(recruitment.getId());
                assertThat(notification.getChatRoom().getId()).isEqualTo(room.getId());
            })
            .extracting(TeamRecruitmentNotification::getMessagePreview)
            .containsExactlyInAnyOrder("첫 번째 메시지", "세 번째 메시지", "네 번째 메시지");
        assertThat(notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(author.getUser().getId()))
            .singleElement()
            .satisfies(notification -> {
                assertThat(notification.getRecipient().getId()).isEqualTo(author.getUser().getId());
                assertThat(notification.getType()).isEqualTo(NEW_CHAT_MESSAGE);
                assertThat(notification.getTargetType()).isEqualTo(CHAT_ROOM);
                assertThat(notification.getRecruitment().getId()).isEqualTo(recruitment.getId());
                assertThat(notification.getChatRoom().getId()).isEqualTo(room.getId());
                assertThat(notification.getMessagePreview()).isEqualTo("두 번째 메시지");
            });
        assertThat(entityManager.createQuery("select count(notification) from TeamRecruitmentNotification notification",
            Long.class).getSingleResult()).isEqualTo(4L);

        List<TeamRecruitmentOutboxEvent> outboxEvents = outboxEventRepository.findAllByStatusOrderByIdAsc(PENDING);
        assertThat(entityManager.createQuery("select count(outbox) from TeamRecruitmentOutboxEvent outbox",
            Long.class).getSingleResult()).isEqualTo(4L);
        assertThat(outboxEvents)
            .hasSize(4)
            .extracting(TeamRecruitmentOutboxEvent::getEventKey)
            .containsExactlyInAnyOrder(
                chatMessageEventKey(firstMessageId, applicant.getUser().getId()),
                chatMessageEventKey(secondMessageId, author.getUser().getId()),
                chatMessageEventKey(thirdMessageId, applicant.getUser().getId()),
                chatMessageEventKey(fourthMessageId, applicant.getUser().getId())
            );
        assertChatNotificationOutbox(
            firstMessageId, applicant.getUser().getId(), author.getUser().getNickname(),
            "첫 번째 메시지", recruitment, room
        );
        assertChatNotificationOutbox(
            secondMessageId, author.getUser().getId(), applicant.getUser().getNickname(),
            "두 번째 메시지", recruitment, room
        );
        assertChatNotificationOutbox(
            thirdMessageId, applicant.getUser().getId(), author.getUser().getNickname(),
            "세 번째 메시지", recruitment, room
        );
        assertChatNotificationOutbox(
            fourthMessageId, applicant.getUser().getId(), author.getUser().getNickname(),
            "네 번째 메시지", recruitment, room
        );

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .param("afterMessageId", firstMessageId.toString())
                .param("limit", "2")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].message_id").value(secondMessageId))
            .andExpect(jsonPath("$[1].message_id").value(thirdMessageId));
        assertThat(chatMemberRepository.findByChatRoom_IdAndUser_Id(room.getId(), applicant.getUser().getId())
            .orElseThrow().getLastReadMessageId()).isEqualTo(thirdMessageId);

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .param("beforeMessageId", fourthMessageId.toString())
                .param("limit", "2")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].message_id").value(secondMessageId))
            .andExpect(jsonPath("$[1].message_id").value(thirdMessageId));
        assertThat(chatMemberRepository.findByChatRoom_IdAndUser_Id(room.getId(), applicant.getUser().getId())
            .orElseThrow().getLastReadMessageId()).isEqualTo(thirdMessageId);

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .param("afterMessageId", firstMessageId.toString())
                .param("beforeMessageId", fourthMessageId.toString())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .param("afterMessageId", thirdMessageId.toString())
                .param("limit", "2")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].message_id").value(fourthMessageId));
        assertThat(chatMemberRepository.findByChatRoom_IdAndUser_Id(room.getId(), applicant.getUser().getId())
            .orElseThrow().getLastReadMessageId()).isEqualTo(fourthMessageId);
    }

    @Test
    @DisplayName("READ_ONLY TEAM 채팅방은 메시지 전송만 차단하고 기존 메시지 조회는 허용한다")
    void readOnlyTeamChatIsReadableButNotWritable() throws Exception {
        TeamRecruitment recruitment = saveRecruitment("읽기 전용 채팅");
        TeamRecruitmentChatRoom room = saveTeamRoom(recruitment, READ_ONLY);
        TeamRecruitmentChatMessage existingMessage = chatMessageRepository.save(TeamRecruitmentChatMessage.builder()
            .chatRoom(room)
            .sender(author.getUser())
            .senderNickname(author.getUser().getNickname())
            .content("마감 전 메시지")
            .isImage(false)
            .build());
        entityManager.flush();

        sendMessage(room, recruitment, authorToken, "마감 후 메시지")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_CHAT_READ_ONLY"));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].message_id").value(existingMessage.getId()))
            .andExpect(jsonPath("$[0].content").value("마감 전 메시지"));
    }

    private void assertChatNotificationOutbox(
        Integer messageId,
        Integer recipientId,
        String senderNickname,
        String messagePreview,
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom room
    ) throws Exception {
        TeamRecruitmentOutboxEvent outbox = outboxEventRepository
            .findByEventKey(chatMessageEventKey(messageId, recipientId))
            .orElseThrow();
        assertThat(outbox.getStatus()).isEqualTo(PENDING);
        assertThat(outbox.getEventType()).isEqualTo("TEAM_RECRUITMENT_NOTIFICATION");
        assertThat(outbox.getAggregateType()).isEqualTo("TEAM_RECRUITMENT");
        assertThat(outbox.getAggregateId()).isEqualTo(recruitment.getId());

        JsonNode payload = objectMapper.readTree(outbox.getPayload());
        assertThat(payload.get("type").asText()).isEqualTo("NEW_CHAT_MESSAGE");
        assertThat(payload.get("target_type").asText()).isEqualTo("CHAT_ROOM");
        assertThat(payload.get("recipient_id").asInt()).isEqualTo(recipientId);
        assertThat(payload.get("recruitment_id").asInt()).isEqualTo(recruitment.getId());
        assertThat(payload.get("application_id").isNull()).isTrue();
        assertThat(payload.get("chat_room_id").asInt()).isEqualTo(room.getId());
        assertThat(payload.get("message_preview").asText()).isEqualTo(messagePreview);

        Integer notificationId = payload.get("notification_id").asInt();
        TeamRecruitmentNotification notification = notificationRepository.findById(notificationId).orElseThrow();
        assertThat(notification.getRecipient().getId()).isEqualTo(recipientId);
        assertThat(notification.getType()).isEqualTo(NEW_CHAT_MESSAGE);
        assertThat(notification.getTargetType()).isEqualTo(CHAT_ROOM);
        assertThat(notification.getRecruitment().getId()).isEqualTo(recruitment.getId());
        assertThat(notification.getApplication()).isNull();
        assertThat(notification.getChatRoom().getId()).isEqualTo(room.getId());
        assertThat(notification.getSenderNickname()).isEqualTo(senderNickname);
        assertThat(notification.getMessagePreview()).isEqualTo(messagePreview);
    }

    private String chatMessageEventKey(Integer messageId, Integer recipientId) {
        return "team-recruitment:chat-message:" + messageId + ":" + recipientId;
    }

    private TeamRecruitment saveRecruitment(String title) {
        return recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(GENERAL)
            .maxParticipants(3)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build());
    }

    private TeamRecruitmentChatRoom saveTeamRoom(
        TeamRecruitment recruitment,
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus status
    ) {
        TeamRecruitmentChatRoom room = chatRoomRepository.save(TeamRecruitmentChatRoom.builder()
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(status)
            .build());
        chatMemberRepository.save(TeamRecruitmentChatMember.builder()
            .chatRoom(room)
            .user(author.getUser())
            .build());
        chatMemberRepository.save(TeamRecruitmentChatMember.builder()
            .chatRoom(room)
            .user(applicant.getUser())
            .build());
        return room;
    }

    private org.springframework.test.web.servlet.ResultActions sendMessage(
        TeamRecruitmentChatRoom room,
        TeamRecruitment recruitment,
        String token,
        String content
    ) throws Exception {
        return mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
                recruitment.getId(), room.getId())
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "content": "%s",
                  "is_image": false
                }
                """.formatted(content)));
    }

}
