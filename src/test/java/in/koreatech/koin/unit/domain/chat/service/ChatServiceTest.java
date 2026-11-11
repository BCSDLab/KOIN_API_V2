package in.koreatech.koin.unit.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatDomainAdapter;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class ChatServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final ChatDomainAdapter lostItem = mock(ChatDomainAdapter.class);
    private final ChatDomainAdapter callvan = mock(ChatDomainAdapter.class);
    private final ChatDomainAdapter recruitment = mock(ChatDomainAdapter.class);
    private final ChatPresenceService presence = mock(ChatPresenceService.class);
    private ChatService service;

    @BeforeEach
    void setUp() {
        when(lostItem.domainType()).thenReturn(ChatDomainType.LOST_ITEM);
        when(callvan.domainType()).thenReturn(ChatDomainType.CALLVAN);
        when(recruitment.domainType()).thenReturn(ChatDomainType.TEAM_RECRUITMENT);
        service = new ChatService(List.of(lostItem, callvan, recruitment), users, presence);
    }

    @Test
    void 모든_도메인의_목록을_최근_메시지_순으로_합친다() {
        login(UserType.STUDENT, true);
        when(lostItem.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("lost-item-10-1", LocalDateTime.of(2026, 10, 6, 10, 0))));
        when(callvan.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("callvan-10-1", null)));
        when(recruitment.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("team-recruitment-10-1", LocalDateTime.of(2026, 10, 6, 11, 0))));

        assertThat(service.getRooms(1, null, SupportedLanguage.KOREAN)).extracting(ChatRoomResponse::roomId)
            .containsExactly("team-recruitment-10-1", "lost-item-10-1", "callvan-10-1");
    }

    @Test
    void 같은_저장_초의_방은_식별자로_정렬하고_반환_시각은_유지한다() {
        login(UserType.STUDENT, true);
        LocalDateTime second = LocalDateTime.of(2026, 10, 12, 21, 45, 13);
        when(lostItem.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("lost-item-10-1", second.withNano(294527000))));
        when(callvan.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("callvan-10-1", second)));
        when(recruitment.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of(room("team-recruitment-10-1", second)));

        var result = service.getRooms(1, null, SupportedLanguage.KOREAN);

        assertThat(result).extracting(ChatRoomResponse::roomId)
            .containsExactly("callvan-10-1", "lost-item-10-1", "team-recruitment-10-1");
        assertThat(result.get(1).lastMessageAt()).isEqualTo(second.withNano(294527000));
    }

    @Test
    void 일반_사용자의_전체_목록은_학생_전용_도메인을_제외한다() {
        login(UserType.GENERAL, true);
        when(lostItem.getRooms(1, SupportedLanguage.KOREAN)).thenReturn(List.of());
        assertThat(service.getRooms(1, null, SupportedLanguage.KOREAN)).isEmpty();
        verify(callvan, org.mockito.Mockito.never()).getRooms(1, SupportedLanguage.KOREAN);
        verify(recruitment, org.mockito.Mockito.never()).getRooms(1, SupportedLanguage.KOREAN);
    }

    @Test
    void 도메인_필터는_해당_도메인만_조회한다() {
        login(UserType.STUDENT, true);
        when(callvan.getRooms(1, SupportedLanguage.ENGLISH)).thenReturn(List.of());
        service.getRooms(1, ChatDomainType.CALLVAN, SupportedLanguage.ENGLISH);
        verify(callvan).getRooms(1, SupportedLanguage.ENGLISH);
        verify(lostItem, org.mockito.Mockito.never()).getRooms(1, SupportedLanguage.ENGLISH);
        verify(recruitment, org.mockito.Mockito.never()).getRooms(1, SupportedLanguage.ENGLISH);
    }

    @Test
    void 학생이_아닌_사용자는_팀_모집_단건과_필터_접근이_거부된다() {
        login(UserType.GENERAL, true);
        assertForbidden(() -> service.getRooms(1, ChatDomainType.TEAM_RECRUITMENT, SupportedLanguage.KOREAN));
        assertForbidden(() -> service.getRoom(1, "team-recruitment-10-1", SupportedLanguage.KOREAN));
        verify(recruitment, org.mockito.Mockito.never()).getRooms(1, SupportedLanguage.KOREAN);
        verify(recruitment, org.mockito.Mockito.never()).getRoom(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 익명_요청은_도메인_저장소를_호출하지_않는다() {
        assertThatThrownBy(() -> service.getRooms(null, null, SupportedLanguage.KOREAN)).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> service.getRoom(0, "callvan-10-1", SupportedLanguage.KOREAN)).isInstanceOf(AuthenticationException.class);
        verifyNoInteractions(users);
    }

    @Test
    void 콜밴은_기존처럼_학생_권한과_계정_인증을_검증한다() {
        login(UserType.GENERAL, true);
        ChatRoomKey key = new ChatRoomKey(ChatDomainType.CALLVAN, 10, 1);
        assertForbidden(() -> service.getRoom(1, key.encoded(), SupportedLanguage.KOREAN));
        login(UserType.STUDENT, false);
        assertThatThrownBy(() -> service.getRoom(1, key.encoded(), SupportedLanguage.KOREAN))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_STUDENT));
        verify(callvan, org.mockito.Mockito.never()).getRoom(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 방_접근_검증이_끝난_뒤에만_사용자의_폴링_접속을_갱신한다() {
        login(UserType.STUDENT, true);
        ChatRoomKey key = new ChatRoomKey(ChatDomainType.CALLVAN, 10, 1);
        service.touchPresence(1, key.encoded());
        var order = org.mockito.Mockito.inOrder(callvan, presence);
        order.verify(callvan).validateReadAccess(1, key);
        order.verify(presence).touchPolling(key, 1);
    }

    @Test
    void 참여_권한을_잃어도_자신의_접속_상태는_정리할_수_있다() {
        login(UserType.STUDENT, true);
        ChatRoomKey key = new ChatRoomKey(ChatDomainType.CALLVAN, 10, 1);
        service.leavePresence(1, key.encoded());
        verify(presence).leavePolling(key, 1);
        verify(callvan, org.mockito.Mockito.never()).validateReadAccess(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private void login(UserType type, boolean authed) {
        when(users.getById(1)).thenReturn(User.builder().id(1).userType(type).isAuthed(authed).isDeleted(false).build());
    }

    private ChatRoomResponse room(String id, LocalDateTime lastMessageAt) {
        return ChatRoomResponse.builder().roomId(id).lastMessageAt(lastMessageAt).build();
    }

    private void assertForbidden(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOfSatisfying(CustomException.class,
            error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_USER_TYPE));
    }
}
