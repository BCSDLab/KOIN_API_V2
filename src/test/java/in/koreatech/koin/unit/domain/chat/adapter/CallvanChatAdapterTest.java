package in.koreatech.koin.unit.domain.chat.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.service.CallvanChatService;
import in.koreatech.koin.domain.chat.adapter.CallvanChatAdapter;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CallvanChatAdapterTest {

    private static final ChatRoomKey KEY = new ChatRoomKey(ChatDomainType.CALLVAN, 10, 20);
    private static final CreateChatMessageRequest REQUEST = new CreateChatMessageRequest("hello", false);
    private static final LocalDateTime TIME = LocalDateTime.of(2026, 10, 6, 12, 30);

    @Mock private CallvanParticipantRepository participantRepository;
    @Mock private CallvanChatMessageRepository messageRepository;
    @Mock private CallvanChatService service;
    @InjectMocks private CallvanChatAdapter adapter;

    @org.junit.jupiter.api.BeforeEach
    void delegateAuthorizationToDomainService() {
        var domainService = new CallvanChatService(null, participantRepository, messageRepository, null, null, null);
        lenient().when(service.getAccessiblePost(anyInt(), anyInt(), anyInt())).thenAnswer(invocation ->
            domainService.getAccessiblePost(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)));
    }

    @Test
    void listExcludesLeftMembershipsMissingAndDeletedRoomsAndPosts() {
        CallvanParticipant left = participant();
        left.leaveCallvan();
        CallvanParticipant missing = participant();
        missing.getPost().updateChatRoom(null);
        CallvanParticipant deletedRoom = participant();
        ReflectionTestUtils.setField(deletedRoom.getPost().getChatRoom(), "isDeleted", true);
        CallvanParticipant deletedPost = participant();
        ReflectionTestUtils.setField(deletedPost.getPost(), "isDeleted", true);
        when(participantRepository.findActiveWithChatRoomByMemberId(1))
            .thenReturn(List.of(left, missing, deletedRoom, deletedPost));

        assertThat(adapter.getRooms(1, SupportedLanguage.KOREAN)).isEmpty();
        verifyNoInteractions(messageRepository);
    }

    @Test
    void inactiveParticipantCannotReadDetailMessagesOrSend() {
        assertDenied(KEY);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void forgedRoomIdCannotReadOrSendEvenWithActiveMembership() {
        authorize();
        assertDenied(new ChatRoomKey(ChatDomainType.CALLVAN, 10, 99));
        verifyNoInteractions(messageRepository);
    }

    @Test
    void leftMembershipCannotReadOrSendEvenIfReturnedByRepository() {
        CallvanParticipant participant = participant();
        participant.leaveCallvan();
        when(participantRepository.findActiveWithChatRoomByPostIdAndMemberId(10, 1))
            .thenReturn(Optional.of(participant));
        assertDenied(KEY);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void missingRoomCannotReadOrSend() {
        CallvanParticipant participant = participant();
        participant.getPost().updateChatRoom(null);
        when(participantRepository.findActiveWithChatRoomByPostIdAndMemberId(10, 1))
            .thenReturn(Optional.of(participant));
        assertDenied(KEY);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void wrongDomainCannotUseCallvanAdapter() {
        assertDenied(new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 20));
        verifyNoInteractions(participantRepository, messageRepository, service);
    }

    @Test
    void deletedRoomCannotReadOrSend() {
        CallvanParticipant participant = participant();
        ReflectionTestUtils.setField(participant.getPost().getChatRoom(), "isDeleted", true);
        when(participantRepository.findActiveWithChatRoomByPostIdAndMemberId(10, 1))
            .thenReturn(Optional.of(participant));
        assertDenied(KEY);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void firstPageUsesLatestLimitedMessagesAndPreservesPersistedFields() {
        authorize();
        List<CallvanChatMessage> page = List.of(message(2, true), message(3, false));
        when(messageRepository.findByChatRoomIdOrderByIdDesc(20, PageRequest.of(0, 2)))
            .thenReturn(page);

        var messages = adapter.getMessages(1, KEY, new ChatMessageQuery(null, null, 2));

        assertThat(messages).extracting(ChatMessageResponse::messageId).containsExactly("2", "3");
        assertThat(messages.get(0).timestamp()).isEqualTo(TIME);
        assertThat(messages.get(0).isLeftUser()).isTrue();
        assertThat(messages.get(0).unreadCount()).isZero();
        verify(participantRepository, never()).advanceReadCursor(anyInt(), anyInt(), anyInt());
        verify(service, never()).markRead(anyInt(), anyInt(), anyInt(), anyLong());
        assertThat(messages.get(0).userId()).isEqualTo(1);
        assertThat(messages.get(0).userNickname()).isEqualTo("nickname");
        verify(service).getAccessiblePost(1, 10, 20);
    }

    @Test
    void pageReadsActiveCursorsOnceAndCountsOtherUnreadMembersInMemory() {
        authorize();
        when(messageRepository.findByChatRoomIdOrderByIdDesc(20, PageRequest.of(0, 200)))
            .thenReturn(java.util.stream.IntStream.rangeClosed(1, 200)
                .mapToObj(id -> message(id, false)).toList());
        when(participantRepository.findActiveReadCursorsByPostId(10))
            .thenReturn(List.of(cursor(1, 0), cursor(2, 0), cursor(3, 100), cursor(4, 200)));

        var messages = adapter.getMessages(1, KEY, new ChatMessageQuery(null, null, 200));

        assertThat(messages).hasSize(200);
        assertThat(messages.subList(0, 100)).extracting(ChatMessageResponse::unreadCount).containsOnly(1);
        assertThat(messages.subList(100, 200)).extracting(ChatMessageResponse::unreadCount).containsOnly(2);
        verify(participantRepository).findActiveReadCursorsByPostId(10);
        verify(messageRepository).findByChatRoomIdOrderByIdDesc(20, PageRequest.of(0, 200));
        verify(participantRepository, never()).countUnreadParticipants(anyInt(), any(), anyInt());
        verifyNoMoreInteractions(messageRepository);
    }

    private CallvanParticipantRepository.ReadCursor cursor(int memberId, int messageId) {
        return new CallvanParticipantRepository.ReadCursor() {
            public Integer getMemberId() {
                return memberId;
            }
            public Integer getLastReadMessageId() {
                return messageId;
            }
        };
    }

    @Test
    void afterCursorReadsNextMessagesInAscendingOrder() {
        authorize();
        List<CallvanChatMessage> page = List.of(message(5, false), message(7, false));
        when(messageRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(20, 3, PageRequest.of(0, 2)))
            .thenReturn(page);
        assertThat(adapter.getMessages(1, KEY, new ChatMessageQuery(3L, null, 2)))
            .extracting(ChatMessageResponse::messageId).containsExactly("5", "7");
    }

    @Test
    void beforeCursorReturnsClosestPreviousMessagesInAscendingOrder() {
        authorize();
        List<CallvanChatMessage> page = List.of(message(7, false), message(5, false));
        when(messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(20, 9, PageRequest.of(0, 2)))
            .thenReturn(page);
        assertThat(adapter.getMessages(1, KEY, new ChatMessageQuery(null, 9L, 2)))
            .extracting(ChatMessageResponse::messageId).containsExactly("5", "7");
    }

    @Test
    void oversizedJpaCursorIsRejectedBeforeMessageQuery() {
        authorize();
        assertThatThrownBy(() -> adapter.getMessages(1, KEY,
            new ChatMessageQuery((long) Integer.MAX_VALUE + 1, null, 2)))
            .isInstanceOf(CustomException.class);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void readReceiptsDelegateToPersistentDomainService() {
        authorize();
        adapter.markRead(1, KEY, 7L);
        verify(service).markRead(1, 10, 20, 7L);
    }

    @Test
    void roomReportsPersistentUnreadCountAndReceiptSupport() {
        authorize();
        when(messageRepository.countUnreadMessages(20, 1)).thenReturn(3);
        var room = adapter.getRoom(1, KEY, SupportedLanguage.KOREAN);
        assertThat(room.supportsReadReceipts()).isTrue();
        assertThat(room.unreadMessageCount()).isEqualTo(3);
        assertThat(room.roomName()).endsWith("(14:05) (4명)");
        assertThat(adapter.getRoom(1, KEY, SupportedLanguage.ENGLISH).roomName())
            .endsWith("(14:05) (4 people)");
        when(participantRepository.findActiveWithChatRoomByMemberId(1)).thenReturn(List.of(participant()));
        assertThat(adapter.getRooms(1, SupportedLanguage.JAPANESE)).singleElement()
            .satisfies(localized -> assertThat(localized.roomName()).endsWith("(14:05) (4人)"));
        verify(participantRepository, never()).advanceReadCursor(anyInt(), anyInt(), anyInt());
    }

    private void assertDenied(ChatRoomKey key) {
        assertThatThrownBy(() -> adapter.getRoom(1, key, SupportedLanguage.KOREAN)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.getMessages(1, key, new ChatMessageQuery(null, null, 2)))
            .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.sendMessage(1, key, REQUEST)).isInstanceOf(CustomException.class);
    }

    private void authorize() {
        when(participantRepository.findActiveWithChatRoomByPostIdAndMemberId(10, 1))
            .thenReturn(Optional.of(participant()));
    }

    private CallvanParticipant participant() {
        CallvanPost post = CallvanPost.builder()
            .departureType(CallvanLocation.FRONT_GATE).arrivalType(CallvanLocation.FRONT_GATE)
            .departureCustomName("출발지").arrivalCustomName("도착지")
            .departureTime(LocalTime.of(14, 5)).maxParticipants(4).currentParticipants(2).build();
        ReflectionTestUtils.setField(post, "id", 10);
        CallvanChatRoom room = CallvanChatRoom.builder().post(post).roomName("stored name").build();
        ReflectionTestUtils.setField(room, "id", 20);
        post.updateChatRoom(room);
        return CallvanParticipant.builder().post(post).build();
    }

    private CallvanChatMessage message(int id, boolean left) {
        User sender = User.builder().id(1).isDeleted(false).build();
        CallvanChatMessage message = CallvanChatMessage.builder().sender(sender)
            .senderNickname("nickname").content("hello").isImage(false).isLeftUser(left).build();
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", TIME);
        return message;
    }
}
