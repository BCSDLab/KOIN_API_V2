package in.koreatech.koin.socket.session.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import lombok.Getter;

@Getter
public class WebSocketUserSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;

    private String deviceToken;

    private WebSocketUserSessionStatus status;

    private Long currentArticleId;

    private Long currentChatRoomId;

    private LocalDateTime lastActiveAt;

    @JsonCreator
    private WebSocketUserSession(
        @JsonProperty("userId") Integer userId,
        @JsonProperty("deviceId") String deviceToken,
        @JsonProperty("status") WebSocketUserSessionStatus status,
        @JsonProperty("currentArticleId") Long currentArticleId,
        @JsonProperty("currentChatRoomId") Long currentChatRoomId,
        @JsonProperty("lastActiveAt") LocalDateTime lastActiveAt
    ) {
        validate(userId, deviceToken, status, lastActiveAt);

        this.userId = userId;
        this.deviceToken = deviceToken;
        this.status = status;
        this.currentArticleId = currentArticleId;
        this.currentChatRoomId = currentChatRoomId;
        this.lastActiveAt = lastActiveAt;
    }

    public static WebSocketUserSession of(Integer userId, String deviceToken) {
        return new WebSocketUserSession(userId, deviceToken, WebSocketUserSessionStatus.ACTIVE_APP, -1L, -1L, LocalDateTime.now());
    }

    public void updateStatus(WebSocketUserSessionStatus status, Long currentArticleId, Long currentChatRoomId) {
        validate(userId, deviceToken, status, lastActiveAt);

        if (status.equals(WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM) && (currentArticleId == null || currentArticleId <= 0)
            && (currentChatRoomId == null || currentChatRoomId <= 0)) {
            throw new KoinIllegalArgumentException("ACTIVE_CHAT_ROOM 상태에서 채팅방 ID는 null 혹은 0을 포함한 음수를 허용하지 않습니다.");
        }

        this.status = status;
        this.currentArticleId = currentArticleId;
        this.currentChatRoomId = currentChatRoomId;
        updateLastActiveAt();
    }

    public void updateLastActiveAt() {
        this.lastActiveAt = LocalDateTime.now();
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        // 가변 요소를 방어적으로 복사
        this.userId = Integer.valueOf(userId);
        this.deviceToken = String.copyValueOf(deviceToken.toCharArray());
        this.status = WebSocketUserSessionStatus.valueOf(status.name());
        this.lastActiveAt = LocalDateTime.of(lastActiveAt.toLocalDate(), lastActiveAt.toLocalTime());
        this.currentArticleId = this.currentArticleId == null ? -1L : currentArticleId;
        this.currentChatRoomId = this.currentChatRoomId == null ? -1L : currentChatRoomId;

        // 불변식을 만족하는지 검사
        validate(userId, deviceToken, status, lastActiveAt);
    }

    private void validate(Integer userId, String deviceToken, WebSocketUserSessionStatus status, LocalDateTime lastActiveAt) {
        if (userId == null) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 에러 : userId는 null일 수 없습니다.");
        }
        if (deviceToken == null) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 에러 : deviceToken은 null일 수 없습니다.");
        }
        if (status == null) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 에러 : status는 null일 수 없습니다.");
        }
        if (lastActiveAt == null) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 에러 : lastActiveAt은 null일 수 없습니다.");
        }
    }
}
