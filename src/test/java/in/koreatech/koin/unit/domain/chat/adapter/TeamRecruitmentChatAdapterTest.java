package in.koreatech.koin.unit.domain.chat.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.chat.adapter.TeamRecruitmentChatAdapter;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.model.ChatRoomType;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentChatRoomListItemResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentChatService;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentChatAdapterTest {

    private static final int USER_ID = 7;
    private static final ChatRoomKey KEY = new ChatRoomKey(ChatDomainType.TEAM_RECRUITMENT, 17, 31);
    private static final LocalDateTime TIME = LocalDateTime.of(2026, 10, 6, 12, 30);

    @Mock
    private TeamRecruitmentChatService service;

    @InjectMocks
    private TeamRecruitmentChatAdapter adapter;

    @ParameterizedTest
    @ValueSource(strings = {"TEAM", "DIRECT"})
    void authorizesDetailBeforeCombiningOnlyMatchingListItem(String type) {
        var counterpart = type.equals("DIRECT") ? new ChatRoomResponse.Counterpart(22, "other") : null;
        when(service.getChatRoom(USER_ID, 17, 31))
            .thenReturn(new ChatRoomResponse(31, "room", type, 2, 6, counterpart));
        var otherContext = new TeamRecruitmentChatRoomListItemResponse(
            99, 31, "wrong", type, null, null, null, "wrong preview", TIME, false, 99);
        when(service.getChatRooms(USER_ID)).thenReturn(List.of(otherContext, room(type)));

        var result = adapter.getRoom(USER_ID, KEY, SupportedLanguage.KOREAN);

        assertThat(result.memberCount()).isEqualTo(2);
        assertThat(result.maxMemberCount()).isEqualTo(6);
        assertThat(result.roomType()).isEqualTo(type.equals("TEAM") ? ChatRoomType.GROUP : ChatRoomType.DIRECT);
        assertThat(result.unreadMessageCount()).isEqualTo(3);
        assertThat(result.lastMessageId()).isEqualTo("901");
        assertThat(result.lastMessageIsImage()).isFalse();
        assertThat(result.lastMessageContent()).isEqualTo("preview");
        var order = inOrder(service);
        order.verify(service).getChatRoom(USER_ID, 17, 31);
        order.verify(service).getChatRooms(USER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = ApiResponseCode.class, names = {"TEAM_RECRUITMENT_CHAT_FORBIDDEN", "TEAM_RECRUITMENT_CHAT_NOT_FOUND"})
    void propagatesAuthorizationAndBindingErrorsWithoutListingRooms(ApiResponseCode code) {
        var error = CustomException.of(code);
        when(service.getChatRoom(USER_ID, 17, 31)).thenThrow(error);

        assertThatThrownBy(() -> adapter.getRoom(USER_ID, KEY, SupportedLanguage.KOREAN)).isSameAs(error);
        verify(service).getChatRoom(USER_ID, 17, 31);
        verifyNoMoreInteractions(service);
    }

    @Test
    void rejectsDetailWhenMatchingMembershipDisappears() {
        when(service.getChatRoom(USER_ID, 17, 31))
            .thenReturn(new ChatRoomResponse(31, "room", "TEAM", 2, 6, null));
        when(service.getChatRooms(USER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> adapter.getRoom(USER_ID, KEY, SupportedLanguage.KOREAN))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsCursorOverflowBeforeCallingService(boolean after) {
        long overflow = (long) Integer.MAX_VALUE + 1;
        var query = new ChatMessageQuery(after ? overflow : null, after ? null : overflow, 20);

        assertThatThrownBy(() -> adapter.getMessages(USER_ID, KEY, query))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
        verifyNoInteractions(service);
    }

    @Test
    void acceptsLargestJpaCursorAndPreservesEmptyPage() {
        when(service.getMessagesWithoutMarkingRead(USER_ID, 17, 31, Integer.MAX_VALUE, null, 200)).thenReturn(List.of());

        assertThat(adapter.getMessages(USER_ID, KEY,
            new ChatMessageQuery((long) Integer.MAX_VALUE, null, 200))).isEmpty();
        verify(service).getMessagesWithoutMarkingRead(USER_ID, 17, 31, Integer.MAX_VALUE, null, 200);
        verifyNoMoreInteractions(service);
    }

    @Test
    void commonSendUsesExplicitSendWithoutMarkingRead() {
        var request = new in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest("hello", false);
        var domainRequest = new in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest("hello", false);
        when(service.createMessageWithoutMarkingRead(USER_ID, 17, 31, domainRequest))
            .thenReturn(new in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse(
                902, USER_ID, "sender", "hello", TIME, false, 1));

        var result = adapter.sendMessage(USER_ID, KEY, request);

        assertThat(result.messageId()).isEqualTo("902");
        assertThat(result.content()).isEqualTo("hello");
        assertThat(result.unreadCount()).isEqualTo(1);
        verify(service).createMessageWithoutMarkingRead(USER_ID, 17, 31, domainRequest);
        verifyNoMoreInteractions(service);
    }

    private TeamRecruitmentChatRoomListItemResponse room(String type) {
        return new TeamRecruitmentChatRoomListItemResponse(17, 31, "room", type,
            type.equals("DIRECT") ? 22 : null, type.equals("DIRECT") ? "other" : null,
            901, "preview", TIME, false, 3);
    }
}
