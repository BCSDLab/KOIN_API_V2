package in.koreatech.koin.acceptance.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.chat.adapter.TeamRecruitmentChatAdapter;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineScheduler;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class TeamRecruitmentChatRepositoryTest extends AcceptanceTest {

    @MockBean private TeamRecruitmentDeadlineScheduler deadlineScheduler;
    @Autowired private TeamRecruitmentChatAdapter adapter;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private TeamRecruitmentChatMemberRepository members;
    @Autowired private TeamRecruitmentChatMessageRepository messages;

    @AfterEach
    void cleanUp() {
        clear();
    }

    @Test
    void cursorUpdatesNeverRegressAndPaginationStaysInsideRoom() {
        User user = User.builder().nickname("member").loginPw("test-password")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build();
        entityManager.persist(user);
        TeamRecruitmentChatRoom room = room(user);
        TeamRecruitmentChatRoom other = room(user);
        members.save(TeamRecruitmentChatMember.builder().chatRoom(room).user(user).build());
        TeamRecruitmentChatMessage first = message(room, user, "first");
        TeamRecruitmentChatMessage second = message(room, user, "second");
        TeamRecruitmentChatMessage third = message(room, user, "third");
        TeamRecruitmentChatMessage foreign = message(other, user, "foreign");
        entityManager.flush();
        Integer roomId = room.getId();
        Integer userId = user.getId();
        assertThat(members.advanceReadCursor(roomId, userId, third.getId())).isEqualTo(1);
        assertThat(members.advanceReadCursor(roomId, userId, first.getId())).isZero();
        assertThat(members.advanceReadCursor(roomId, userId, third.getId())).isZero();
        assertThat(members.findByChatRoom_IdAndUser_Id(roomId, userId).orElseThrow().getLastReadMessageId())
            .isEqualTo(third.getId());
        assertThat(messages.existsByIdAndChatRoom_Id(foreign.getId(), roomId)).isFalse();
        assertThat(messages.findAllByChatRoom_IdOrderByIdDesc(roomId, PageRequest.of(0, 2)))
            .extracting(TeamRecruitmentChatMessage::getId).containsExactly(third.getId(), second.getId());
        assertThat(messages.findAllByChatRoom_IdAndIdGreaterThanOrderByIdAsc(roomId, first.getId(), PageRequest.of(0, 1)))
            .extracting(TeamRecruitmentChatMessage::getId).containsExactly(second.getId());
        assertThat(messages.findAllByChatRoom_IdAndIdLessThanOrderByIdDesc(roomId, third.getId(), PageRequest.of(0, 1)))
            .extracting(TeamRecruitmentChatMessage::getId).containsExactly(second.getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void mixedRoomListQueryCountStaysBoundedAsRoomsGrowAndPreservesWithdrawnCanSend() {
        clear();
        var transaction = new TransactionTemplate(transactionManager);
        var small = transaction.execute(status -> mixedRooms(1));
        var large = transaction.execute(status -> mixedRooms(4));
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();

        // Each service call must open its own transaction, just as it does behind the adapter in production.
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        statistics.clear();
        var smallRooms = adapter.getRooms(small.userId(), SupportedLanguage.KOREAN);
        long smallQueries = statistics.getPrepareStatementCount();
        statistics.clear();
        var largeRooms = adapter.getRooms(large.userId(), SupportedLanguage.KOREAN);
        long largeQueries = statistics.getPrepareStatementCount();

        assertThat(smallRooms).hasSize(4).allSatisfy(room ->
            assertThat(room.canSend()).isEqualTo(small.canSendByRoomId().get(room.chatRoomId())));
        assertThat(largeRooms).hasSize(16).allSatisfy(room ->
            assertThat(room.canSend()).isEqualTo(large.canSendByRoomId().get(room.chatRoomId())));
        assertThat(smallRooms).extracting(ChatRoomResponse::chatRoomId)
            .containsExactlyInAnyOrderElementsOf(small.canSendByRoomId().keySet());
        assertThat(largeRooms).extracting(ChatRoomResponse::chatRoomId)
            .containsExactlyInAnyOrderElementsOf(large.canSendByRoomId().keySet());
        assertThat(smallQueries).isPositive();
        assertThat(largeQueries).as("SELECT count for 16 mixed rooms must stay bounded by the 4-room list")
            .isLessThanOrEqualTo(smallQueries);
    }

    private RoomListFixture mixedRooms(int copies) {
        User viewer = user("viewer-" + copies);
        Map<Integer, Boolean> canSendByRoomId = new LinkedHashMap<>();
        for (int i = 0; i < copies; i++) {
            TeamRecruitmentChatRoom activeTeam = room(viewer);
            TeamRecruitmentChatRoom withdrawnAuthorTeam = room(null);
            members.save(TeamRecruitmentChatMember.builder().chatRoom(activeTeam).user(viewer).build());
            members.save(TeamRecruitmentChatMember.builder().chatRoom(withdrawnAuthorTeam).user(viewer).build());
            canSendByRoomId.put(activeTeam.getId(), true);
            canSendByRoomId.put(withdrawnAuthorTeam.getId(), false);

            // A null applicant is the persisted state after account withdrawal.
            for (boolean withdrawn : new boolean[] {false, true}) {
                User applicant = withdrawn ? null : user("applicant-" + copies + "-" + i);
                TeamRecruitmentApplication application = TeamRecruitmentApplication.builder()
                    .recruitment(activeTeam.getRecruitment()).applicant(applicant)
                    .motivation("study").availability("weekends").profileSnapshot("{}")
                    .status(TeamRecruitmentApplicationStatus.ACCEPTED).build();
                entityManager.persist(application);
                TeamRecruitmentChatRoom direct = TeamRecruitmentChatRoom.builder()
                    .recruitment(activeTeam.getRecruitment()).application(application)
                    .roomScopeKey("DIRECT-" + application.getId()).roomType(TeamRecruitmentChatRoomType.DIRECT)
                    .build();
                entityManager.persist(direct);
                members.save(TeamRecruitmentChatMember.builder().chatRoom(direct).user(viewer).build());
                if (applicant != null) {
                    members.save(TeamRecruitmentChatMember.builder().chatRoom(direct).user(applicant).build());
                }
                canSendByRoomId.put(direct.getId(), !withdrawn);
            }
        }
        entityManager.flush();
        entityManager.clear();
        return new RoomListFixture(viewer.getId(), canSendByRoomId);
    }

    private User user(String nickname) {
        User user = User.builder().nickname(nickname).loginPw("test-password")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build();
        entityManager.persist(user);
        return user;
    }

    private record RoomListFixture(Integer userId, Map<Integer, Boolean> canSendByRoomId) {}

    private TeamRecruitmentChatRoom room(User author) {
        LocalDate date = LocalDate.of(2026, 10, 12);
        TeamRecruitment recruitment = TeamRecruitment.builder().author(author).category(TeamRecruitmentCategory.STUDY)
            .title("study").meetingType(TeamRecruitmentMeetingType.ONLINE).activityStartDate(date)
            .activityEndDate(date.plusDays(10)).deadlineDate(date).recruitmentType(TeamRecruitmentType.GENERAL)
            .maxParticipants(3).description("study together").build();
        entityManager.persist(recruitment);
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.createTeamRoom(recruitment);
        entityManager.persist(room);
        return room;
    }

    private TeamRecruitmentChatMessage message(TeamRecruitmentChatRoom room, User user, String text) {
        return messages.save(TeamRecruitmentChatMessage.builder().chatRoom(room).sender(user)
            .senderNickname("member").content(text).build());
    }
}
