package in.koreatech.koin.unit.domain.callvan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.event.CallvanNewMessageEvent;
import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.callvan.service.CallvanChatService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CallvanChatServiceTest {

    @Mock private CallvanPostRepository postRepository;
    @Mock private CallvanParticipantRepository participantRepository;
    @Mock private CallvanChatMessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private jakarta.persistence.EntityManager entityManager;
    @InjectMocks private CallvanChatService service;

    @Test
    void returnsSavedEntityAndPublishesExactlyOneEvent() {
        CallvanChatRoom room = prepareSender();
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 6, 12, 30);
        CallvanChatMessage persisted = CallvanChatMessage.builder()
            .chatRoom(room).senderNickname("nickname").content("image-url")
            .messageType(CallvanMessageType.IMAGE).isImage(true).build();
        ReflectionTestUtils.setField(persisted, "id", 42);
        ReflectionTestUtils.setField(persisted, "createdAt", createdAt);
        when(messageRepository.save(any())).thenReturn(persisted);

        CallvanChatMessage result = service.sendMessageAndGet(10, 1,
            new CallvanChatMessageRequest("image-url", true));

        assertThat(result).isSameAs(persisted);
        var order = inOrder(postRepository, participantRepository, messageRepository);
        order.verify(postRepository).findByIdWithLock(10);
        order.verify(participantRepository).existsByPostIdAndMemberIdAndIsDeletedFalse(10, 1);
        verify(participantRepository, times(1)).existsByPostIdAndMemberIdAndIsDeletedFalse(10, 1);
        order.verify(messageRepository).save(any());
        ArgumentCaptor<ChatMessageStoredEvent> stored = ArgumentCaptor.forClass(ChatMessageStoredEvent.class);
        verify(eventPublisher).publishEvent(stored.capture());
        assertThat(stored.getValue().message().messageId()).isEqualTo("42");
        assertThat(stored.getValue().message().timestamp()).isEqualTo(createdAt);
        ArgumentCaptor<CallvanChatMessage> saved = ArgumentCaptor.forClass(CallvanChatMessage.class);
        verify(messageRepository).save(saved.capture());
        assertThat(saved.getValue().getChatRoom()).isSameAs(room);
        assertThat(saved.getValue().getIsImage()).isTrue();
        assertThat(saved.getValue().getSenderNickname()).isEqualTo("nickname");
        verify(eventPublisher).publishEvent(new CallvanNewMessageEvent(10, "nickname", 1,
            "image-url", CallvanMessageType.IMAGE));
        verifyNoMoreInteractions(eventPublisher, messageRepository);
    }

    @Test
    void inactiveParticipantCannotRead() {
        assertThatThrownBy(() -> service.getMessages(10, 1)).isInstanceOf(CustomException.class);
        verifyNoInteractions(postRepository, userRepository, messageRepository, eventPublisher);
    }

    @Test
    void membershipIsCheckedOnlyAfterAcquiringPostLockThroughEitherSendApi() {
        when(postRepository.findByIdWithLock(10)).thenReturn(Optional.of(mock(CallvanPost.class)));

        assertThatThrownBy(() -> service.sendMessage(10, 1, new CallvanChatMessageRequest("hello", false)))
            .isInstanceOf(CustomException.class)
            .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                .isEqualTo(in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_PARTICIPANT));
        assertThatThrownBy(() -> service.sendMessageAndGet(10, 1, new CallvanChatMessageRequest("hello", false)))
            .isInstanceOf(CustomException.class)
            .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                .isEqualTo(in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_PARTICIPANT));

        var order = inOrder(postRepository, participantRepository);
        for (int invocation = 0; invocation < 2; invocation++) {
            order.verify(postRepository).findByIdWithLock(10);
            order.verify(participantRepository).existsByPostIdAndMemberIdAndIsDeletedFalse(10, 1);
        }
        verifyNoMoreInteractions(postRepository, participantRepository);
        verifyNoInteractions(userRepository, messageRepository, eventPublisher);
    }

    @Test
    void nonexistentPostKeepsLegacyForbiddenResponse() {
        assertThatThrownBy(() -> service.sendMessage(999, 1, new CallvanChatMessageRequest("hello", false)))
            .isInstanceOf(CustomException.class)
            .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                .isEqualTo(in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_PARTICIPANT));
        verify(postRepository).findByIdWithLock(999);
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(participantRepository, userRepository, messageRepository, eventPublisher);
    }

    @Test
    void legacyReadKeepsRoomNameDateTimeAndSenderFlags() {
        CallvanPost post = CallvanPost.builder()
            .departureType(CallvanLocation.FRONT_GATE).arrivalType(CallvanLocation.STATION)
            .departureTime(LocalTime.of(14, 5)).maxParticipants(4).build();
        CallvanChatRoom room = CallvanChatRoom.builder().post(post).build();
        ReflectionTestUtils.setField(room, "id", 20);
        post.updateChatRoom(room);
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1);
        CallvanChatMessage message = CallvanChatMessage.builder().sender(sender)
            .senderNickname("nickname").content("hello").isLeftUser(true).build();
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.of(2026, 10, 6, 14, 5));
        when(participantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(10, 1)).thenReturn(true);
        when(postRepository.getById(10)).thenReturn(post);
        when(messageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(20)).thenReturn(List.of(message));

        var result = service.getMessages(10, 1);

        assertThat(result.roomName()).isEqualTo("정문 -> 천안역 (14:05) (4명)");
        assertThat(result.messages()).hasSize(1);
        var response = result.messages().get(0);
        assertThat(response.date()).isEqualTo("2026. 10. 06");
        assertThat(response.time()).isEqualTo("오후 2:05");
        assertThat(response.senderNickname()).isEqualTo("nickname");
        assertThat(response.isMine()).isTrue();
        assertThat(response.isLeftUser()).isTrue();
        verifyNoInteractions(eventPublisher, userRepository);
    }

    @Test
    void invalidReadIdsAreRejectedWithoutPersistence() {
        for (Long id : new Long[] {null, 0L, -1L, (long) Integer.MAX_VALUE + 1}) {
            assertThatThrownBy(() -> service.markRead(1, 10, 20, id))
                .isInstanceOf(CustomException.class);
        }
        verifyNoInteractions(postRepository, participantRepository, messageRepository, eventPublisher);
    }

    @Test
    void readRequiresActiveMembershipAndMatchingRoom() {
        assertThatThrownBy(() -> service.markRead(1, 10, 20, 7L)).isInstanceOf(CustomException.class);
        verifyNoInteractions(messageRepository, eventPublisher);
    }

    @Test
    void readRejectsMissingOrOtherRoomMessage() {
        prepareReader();
        assertThatThrownBy(() -> service.markRead(1, 10, 20, 7L)).isInstanceOf(CustomException.class);
        verify(participantRepository, never()).advanceReadCursor(anyInt(), anyInt(), anyInt());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void onlyAdvancedReadPublishesRoomChanged() {
        prepareReader();
        when(messageRepository.existsByIdAndChatRoomId(7, 20)).thenReturn(true);
        when(participantRepository.advanceReadCursor(10, 1, 7)).thenReturn(1, 0, 0);
        service.markRead(1, 10, 20, 7L);
        service.markRead(1, 10, 20, 7L);
        service.markRead(1, 10, 20, 7L);
        verify(eventPublisher).publishEvent(new in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent(
            new in.koreatech.koin.domain.chat.model.ChatRoomKey(
                in.koreatech.koin.domain.chat.model.ChatDomainType.CALLVAN, 10, 20)));
        verifyNoMoreInteractions(eventPublisher);
    }

    private void prepareReader() {
        CallvanPost post = CallvanPost.builder().build();
        ReflectionTestUtils.setField(post, "id", 10);
        CallvanChatRoom room = CallvanChatRoom.builder().post(post).build();
        ReflectionTestUtils.setField(room, "id", 20);
        post.updateChatRoom(room);
        var participant = in.koreatech.koin.domain.callvan.model.CallvanParticipant.builder().post(post).build();
        when(participantRepository.findActiveWithChatRoomByPostIdAndMemberId(10, 1))
            .thenReturn(java.util.Optional.of(participant));
    }

    private CallvanChatRoom prepareSender() {
        CallvanPost post = mock(CallvanPost.class);
        CallvanChatRoom room = CallvanChatRoom.builder().build();
        ReflectionTestUtils.setField(room, "id", 20);
        User sender = mock(User.class);
        when(participantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(10, 1)).thenReturn(true);
        when(postRepository.findByIdWithLock(10)).thenReturn(Optional.of(post));
        when(post.getChatRoom()).thenReturn(room);
        when(post.getId()).thenReturn(10);
        when(userRepository.getById(1)).thenReturn(sender);
        when(sender.getId()).thenReturn(1);
        when(sender.getDisplayNickname()).thenReturn("nickname");
        return room;
    }
}
