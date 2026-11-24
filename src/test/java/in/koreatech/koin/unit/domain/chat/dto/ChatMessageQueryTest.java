package in.koreatech.koin.unit.domain.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.global.exception.CustomException;

class ChatMessageQueryTest {

    @Test
    void 커서가_없으면_최신_limit개를_오름차순으로_반환한다() {
        assertThat(new ChatMessageQuery(null, null, 2).select(messages()))
            .extracting(ChatMessageResponse::messageId).containsExactly("4", "5");
    }

    @Test
    void 이후_커서는_배타적으로_새_메시지를_조회한다() {
        assertThat(new ChatMessageQuery(2L, null, 2).select(messages()))
            .extracting(ChatMessageResponse::messageId).containsExactly("3", "4");
    }

    @Test
    void 이전_커서는_바로_이전_limit개를_오름차순으로_반환한다() {
        assertThat(new ChatMessageQuery(null, 5L, 2).select(messages()))
            .extracting(ChatMessageResponse::messageId).containsExactly("3", "4");
    }

    @Test
    void 자바스크립트_정수_범위를_넘는_TSID도_정확하게_비교한다() {
        List<ChatMessageResponse> messages = List.of(message("9007199254740993"), message("9007199254740992"));
        assertThat(new ChatMessageQuery(9007199254740992L, null, 100).select(messages))
            .extracting(ChatMessageResponse::messageId).containsExactly("9007199254740993");
    }

    @Test
    void 커서가_범위_밖이거나_메시지가_없으면_빈_목록을_반환한다() {
        assertThat(new ChatMessageQuery(5L, null, 100).select(messages())).isEmpty();
        assertThat(new ChatMessageQuery(null, 1L, 100).select(messages())).isEmpty();
        assertThat(new ChatMessageQuery(null, null, 100).select(List.of())).isEmpty();
    }

    @Test
    void 잘못된_커서와_limit은_저장소에_접근하기_전에_거부한다() {
        assertThatThrownBy(() -> new ChatMessageQuery(1L, 2L, 100)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> new ChatMessageQuery(0L, null, 100)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> new ChatMessageQuery(null, -1L, 100)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> new ChatMessageQuery(null, null, 0)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> new ChatMessageQuery(null, null, 201)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> new ChatMessageQuery(Long.MAX_VALUE, null, 100).afterAsInteger())
            .isInstanceOf(CustomException.class);
    }

    private List<ChatMessageResponse> messages() {
        return List.of(message("5"), message("2"), message("4"), message("1"), message("3"));
    }

    private ChatMessageResponse message(String id) {
        return ChatMessageResponse.builder().messageId(id).content(id).build();
    }
}
