package in.koreatech.koin.unit.domain.chat.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class ChatRoomKeyTest {

    @Test
    void 도메인과_게시글이_달라도_동일한_기존_방번호가_충돌하지_않는다() {
        assertThat(Arrays.stream(ChatDomainType.values())
            .map(type -> new ChatRoomKey(type, 10, 1).encoded()))
            .doesNotHaveDuplicates();
        assertThat(new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 1).encoded())
            .isNotEqualTo(new ChatRoomKey(ChatDomainType.LOST_ITEM, 11, 1).encoded());
    }

    @Test
    void 공통_식별자는_기존_도메인의_게시글과_방번호로_복원된다() {
        for (ChatDomainType domainType : ChatDomainType.values()) {
            ChatRoomKey key = new ChatRoomKey(domainType, 10, 20);
            assertThat(ChatRoomKey.parse(key.encoded())).isEqualTo(key);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "1", "lost-item-1", "lost-item-01-1", "lost-item-1-0",
        "lost-item--1-2", "unknown-1-1", "callvan-2147483648-1", "team-recruitment-1-2147483648",
        "lost-item-1-1-extra", "lost-item-1-1\n"})
    void 잘못된_식별자는_400_에러로_처리한다(String value) {
        assertThatThrownBy(() -> ChatRoomKey.parse(value))
            .isInstanceOfSatisfying(CustomException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
    }
}
