package in.koreatech.koin.acceptance.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.callvan.service.CallvanChatService;
import in.koreatech.koin.domain.callvan.service.CallvanNotificationEventListener;
import in.koreatech.koin.domain.callvan.service.CallvanPostJoinService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

class CallvanChatRepositoryTest extends AcceptanceTest {

    @Autowired
    private CallvanParticipantRepository participants;

    @Autowired
    private CallvanChatMessageRepository messages;

    @SpyBean
    private CallvanPostRepository posts;

    @Autowired
    private CallvanPostJoinService joinService;

    @Autowired
    private CallvanChatService chatService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private CallvanNotificationEventListener notificationEventListener;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void joiningUsesMessagesCommittedWhileWaitingForThePostLock() throws Exception {
        var executor = Executors.newSingleThreadExecutor();
        try {
            clear();
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
            transaction.setTimeout(5);
            CallvanPost post = transaction.execute(status -> {
                User sender = user("sender");
                CallvanPost created = post(sender, true);
                participants.save(CallvanParticipant.builder().post(created).member(sender).build());
                message(created.getChatRoom(), sender, "before join");
                return created;
            });
            User reader = transaction.execute(status -> user("reader"));
            CountDownLatch postLocked = new CountDownLatch(1);
            CountDownLatch joinLockRequested = new CountDownLatch(1);
            Thread joiningThread = Thread.currentThread();
            Answer<Object> requestPostLock = invocation -> {
                if (Thread.currentThread() == joiningThread) {
                    joinLockRequested.countDown();
                }
                return mockingDetails(posts).getMockCreationSettings().getDefaultAnswer().answer(invocation);
            };
            doAnswer(requestPostLock).when(posts).getByIdWithLock(post.getId());
            doAnswer(requestPostLock).when(posts).findByIdWithLock(post.getId());

            var sent = executor.submit(() -> transaction.execute(status -> {
                posts.getByIdWithLock(post.getId());
                postLocked.countDown();
                try {
                    assertThat(joinLockRequested.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return chatService.sendMessageAndGet(post.getId(), post.getAuthor().getId(),
                    new CallvanChatMessageRequest("committed before join", false)).getId();
            }));
            assertThat(postLocked.await(10, TimeUnit.SECONDS)).isTrue();
            transaction.executeWithoutResult(status -> joinService.join(post.getId(), reader.getId()));
            Integer latestMessageId = sent.get(10, TimeUnit.SECONDS);

            transaction.executeWithoutResult(status -> {
                assertThat(participants.findByPostIdAndMemberId(post.getId(), reader.getId()).orElseThrow()
                    .getLastReadMessageId()).isEqualTo(latestMessageId);
                assertThat(messages.countUnreadMessages(post.getChatRoom().getId(), reader.getId())).isZero();
            });
            chatService.sendMessageAndGet(post.getId(), post.getAuthor().getId(),
                new CallvanChatMessageRequest("after join", false));
            transaction.executeWithoutResult(status ->
                assertThat(messages.countUnreadMessages(post.getChatRoom().getId(), reader.getId())).isEqualTo(1));
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
            clear();
        }
    }

    @Test
    void 활동중인_참여자의_게시글과_채팅방을_함께_조회한다() {
        User member = user("member");
        CallvanPost active = post(member, true);
        CallvanPost left = post(member, true);
        CallvanPost noRoom = post(member, false);
        participants.save(CallvanParticipant.builder().post(active).member(member).build());
        participants.save(CallvanParticipant.builder().post(left).member(member).isDeleted(true).build());
        participants.save(CallvanParticipant.builder().post(noRoom).member(member).build());
        entityManager.flush();
        entityManager.clear();

        List<CallvanParticipant> result = participants.findActiveWithChatRoomByMemberId(member.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPost().getId()).isEqualTo(active.getId());
        assertThat(result.get(0).getPost().getChatRoom().getId()).isEqualTo(active.getChatRoom().getId());
        assertThat(participants.findActiveWithChatRoomByPostIdAndMemberId(active.getId(), member.getId())).isPresent();
        assertThat(participants.findActiveWithChatRoomByPostIdAndMemberId(left.getId(), member.getId())).isEmpty();
    }

    @Test
    void 다른_사용자나_삭제된_채팅방은_조회하지_않는다() {
        User member = user("member");
        User other = user("other");
        CallvanPost active = post(member, true);
        CallvanPost deleted = post(member, false);
        CallvanChatRoom deletedRoom = CallvanChatRoom.builder().post(deleted).roomName("삭제된 방").isDeleted(true).build();
        entityManager.persist(deletedRoom);
        deleted.updateChatRoom(deletedRoom);
        participants.save(CallvanParticipant.builder().post(active).member(other).build());
        participants.save(CallvanParticipant.builder().post(deleted).member(member).build());
        entityManager.flush();
        entityManager.clear();

        assertThat(participants.findActiveWithChatRoomByMemberId(member.getId())).isEmpty();
        assertThat(participants.findActiveWithChatRoomByPostIdAndMemberId(active.getId(), member.getId())).isEmpty();
    }

    @Test
    void 메시지_쿼리는_방별_커서와_limit을_적용한다() {
        User member = user("member");
        CallvanPost post = post(member, true);
        CallvanPost otherPost = post(member, true);
        CallvanChatMessage first = message(post.getChatRoom(), member, "first");
        CallvanChatMessage second = message(post.getChatRoom(), member, "second");
        CallvanChatMessage third = message(post.getChatRoom(), member, "third");
        message(otherPost.getChatRoom(), member, "other room");
        entityManager.flush();
        entityManager.clear();
        Integer roomId = post.getChatRoom().getId();

        assertThat(messages.findByChatRoomIdOrderByIdDesc(roomId, PageRequest.of(0, 2)))
            .extracting(CallvanChatMessage::getId).containsExactly(third.getId(), second.getId());
        assertThat(messages.findByChatRoomIdOrderByIdAsc(roomId, PageRequest.of(0, 2)))
            .extracting(CallvanChatMessage::getId).containsExactly(first.getId(), second.getId());
        assertThat(messages.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(roomId, first.getId(), PageRequest.of(0, 1)))
            .extracting(CallvanChatMessage::getId).containsExactly(second.getId());
        assertThat(messages.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, third.getId(), PageRequest.of(0, 1)))
            .extracting(CallvanChatMessage::getId).containsExactly(second.getId());
        assertThat(messages.findFirstByChatRoomIdOrderByIdDesc(roomId)).get()
            .extracting(CallvanChatMessage::getId).isEqualTo(third.getId());
        assertThat(messages.findByChatRoomIdOrderByIdAsc(roomId, PageRequest.of(0, 1)).get(0).getCreatedAt()).isNotNull();
    }

    @Test
    void persistedCursorIsMonotonicAndUnreadCountsExcludeSelfAndDepartedMembers() {
        User sender = user("sender");
        User reader = user("reader");
        User departed = user("departed");
        User withdrawn = user("withdrawn");
        org.springframework.test.util.ReflectionTestUtils.setField(withdrawn, "isDeleted", true);
        CallvanPost post = post(sender, true);
        participants.save(CallvanParticipant.builder().post(post).member(sender).build());
        participants.save(CallvanParticipant.builder().post(post).member(reader).build());
        participants.save(CallvanParticipant.builder().post(post).member(departed).isDeleted(true).build());
        participants.save(CallvanParticipant.builder().post(post).member(withdrawn).build());
        CallvanChatMessage first = message(post.getChatRoom(), sender, "first");
        CallvanChatMessage own = message(post.getChatRoom(), reader, "own");
        CallvanChatMessage last = message(post.getChatRoom(), sender, "last");
        CallvanPost other = post(sender, true);
        CallvanChatMessage foreign = message(other.getChatRoom(), sender, "foreign");
        entityManager.flush();
        Integer postId = post.getId();
        Integer roomId = post.getChatRoom().getId();

        assertThat(participants.findActiveReadCursorsByPostId(postId))
            .extracting(CallvanParticipantRepository.ReadCursor::getMemberId)
            .containsExactlyInAnyOrder(sender.getId(), reader.getId());
        assertThat(messages.countUnreadMessages(roomId, reader.getId())).isEqualTo(2);
        assertThat(participants.countUnreadParticipants(postId, sender.getId(), last.getId())).isEqualTo(1);
        assertThat(messages.existsByIdAndChatRoomId(foreign.getId(), roomId)).isFalse();
        assertThat(participants.advanceReadCursor(postId, reader.getId(), last.getId())).isEqualTo(1);
        assertThat(participants.advanceReadCursor(postId, reader.getId(), first.getId())).isZero();
        assertThat(participants.advanceReadCursor(postId, reader.getId(), last.getId())).isZero();
        assertThat(participants.advanceReadCursor(postId, departed.getId(), last.getId())).isZero();
        assertThat(participants.findByPostIdAndMemberId(postId, reader.getId()).orElseThrow()
            .getLastReadMessageId()).isEqualTo(last.getId());
        assertThat(participants.findActiveReadCursorsByPostId(postId))
            .extracting(CallvanParticipantRepository.ReadCursor::getMemberId,
                CallvanParticipantRepository.ReadCursor::getLastReadMessageId)
            .containsExactlyInAnyOrder(org.assertj.core.api.Assertions.tuple(sender.getId(), 0),
                org.assertj.core.api.Assertions.tuple(reader.getId(), last.getId()));
        assertThat(messages.countUnreadMessages(roomId, reader.getId())).isZero();
        assertThat(messages.countUnreadMessages(roomId, sender.getId())).isEqualTo(1);
        assertThat(participants.countUnreadParticipants(postId, sender.getId(), last.getId())).isZero();
    }

    private User user(String nickname) {
        User user = User.builder().nickname(nickname).loginPw("test-password")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build();
        entityManager.persist(user);
        return user;
    }

    private CallvanPost post(User author, boolean withRoom) {
        CallvanPost post = CallvanPost.builder().author(author).title("콜밴")
            .departureType(CallvanLocation.FRONT_GATE).arrivalType(CallvanLocation.STATION)
            .departureDate(LocalDate.now().plusDays(1)).departureTime(LocalTime.of(14, 0))
            .maxParticipants(4).build();
        entityManager.persist(post);
        if (withRoom) {
            CallvanChatRoom room = CallvanChatRoom.builder().post(post).roomName("콜밴 채팅").build();
            entityManager.persist(room);
            post.updateChatRoom(room);
        }
        return post;
    }

    private CallvanChatMessage message(CallvanChatRoom room, User sender, String content) {
        return messages.save(CallvanChatMessage.builder().chatRoom(room).sender(sender)
            .senderNickname(sender.getDisplayNickname()).content(content).build());
    }
}
