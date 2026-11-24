package in.koreatech.koin.unit.domain.callvan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.callvan.service.CallvanPostJoinService;
import in.koreatech.koin.domain.callvan.service.CallvanRestrictionService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class CallvanPostJoinServiceTest {

    @Mock private CallvanPostRepository postRepository;
    @Mock private CallvanParticipantRepository participantRepository;
    @Mock private CallvanChatMessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private EntityManager entityManager;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CallvanRestrictionService restrictionService;
    @InjectMocks private CallvanPostJoinService service;

    @Test
    void newMemberStartsAtLatestMessage() {
        prepareJoin();
        CallvanChatMessage last = CallvanChatMessage.builder().build();
        ReflectionTestUtils.setField(last, "id", 42);
        when(messageRepository.findFirstByChatRoomIdOrderByIdDesc(20)).thenReturn(Optional.of(last));

        service.join(10, 2);

        ArgumentCaptor<CallvanParticipant> saved = ArgumentCaptor.forClass(CallvanParticipant.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getLastReadMessageId()).isEqualTo(42);
    }

    @Test
    void newMemberInEmptyRoomStartsAtZero() {
        prepareJoin();
        service.join(10, 2);
        ArgumentCaptor<CallvanParticipant> saved = ArgumentCaptor.forClass(CallvanParticipant.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getLastReadMessageId()).isZero();
    }

    @Test
    void rejoinRetainsPreviousCursor() {
        CallvanPost post = prepareJoin();
        CallvanParticipant participant = CallvanParticipant.builder().post(post)
            .lastReadMessageId(7).isDeleted(true).build();
        when(participantRepository.existsByPostIdAndMemberIdAndIsDeletedTrue(10, 2)).thenReturn(true);
        when(participantRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.of(participant));

        service.join(10, 2);

        assertThat(participant.getIsDeleted()).isFalse();
        assertThat(participant.getLastReadMessageId()).isEqualTo(7);
        verify(messageRepository, never()).findFirstByChatRoomIdOrderByIdDesc(anyInt());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void restrictionErrorStillTakesPriorityWhenThePostDoesNotExist() {
        doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_CALLVAN_RESTRICTED_USER))
            .when(restrictionService).validateNotRestricted(2);

        assertThatThrownBy(() -> service.join(10, 2))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_CALLVAN_RESTRICTED_USER));
        verify(postRepository).findByIdWithLock(10);
        verifyNoInteractions(participantRepository, messageRepository, userRepository, eventPublisher);
    }

    @Test
    void unrestrictedUserStillGetsNotFoundForAMissingPost() {
        assertThatThrownBy(() -> service.join(10, 2))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ARTICLE));
        verify(restrictionService).validateNotRestricted(2);
        verifyNoInteractions(participantRepository, messageRepository, userRepository, eventPublisher);
    }

    @Test
    void creatorStillCannotLeave() {
        CallvanPost post = post();
        when(postRepository.getByIdWithLock(10)).thenReturn(post);
        assertThatThrownBy(() -> service.leave(10, 1))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.CALLVAN_POST_AUTHOR));
        verifyNoInteractions(participantRepository, messageRepository, eventPublisher);
    }

    private CallvanPost prepareJoin() {
        CallvanPost post = post();
        when(postRepository.findByIdWithLock(10)).thenReturn(Optional.of(post));
        when(userRepository.getById(2)).thenReturn(User.builder().id(2).nickname("member").isDeleted(false).build());
        return post;
    }

    private CallvanPost post() {
        CallvanPost post = CallvanPost.builder().author(User.builder().id(1).isDeleted(false).build())
            .departureDate(LocalDate.now().plusDays(1)).departureTime(LocalTime.NOON).maxParticipants(4).build();
        ReflectionTestUtils.setField(post, "id", 10);
        CallvanChatRoom room = CallvanChatRoom.builder().post(post).build();
        ReflectionTestUtils.setField(room, "id", 20);
        post.updateChatRoom(room);
        return post;
    }
}
