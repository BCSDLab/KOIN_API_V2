package in.koreatech.koin.socket.session.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.socket.session.model.WebSocketUserSession;
import in.koreatech.koin.socket.session.repository.WebSocketUserSessionRedisRepository;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketUserSessionService {

    private final WebSocketUserSessionRedisRepository userSessionRepository;

    public void save(Integer userId, WebSocketUserSession value) {
        userSessionRepository.save(userId, value);
    }

    public Optional<WebSocketUserSession> read(Integer userId) {
        return userSessionRepository.findUserSession(userId);
    }

    public boolean exists(Integer userId) {
        return userSessionRepository.exists(userId);
    }

    public WebSocketUserSession updateUserStatus(Integer userId, WebSocketUserSessionStatus status) {
        return updateUserStatus(userId, -1L, -1L, status);
    }

    public WebSocketUserSession updateUserStatus(Integer userId, Long articleId, Long chatRoomId) {
        return updateUserStatus(userId, articleId, chatRoomId, WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM);
    }

    private WebSocketUserSession updateUserStatus(Integer userId, Long articleId, Long chatRoomId, WebSocketUserSessionStatus status) {
        WebSocketUserSession webSocketUserSession = userSessionRepository.findUserSession(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.INVALID_WEBSOCKET_USER_SESSION));

        webSocketUserSession.updateStatus(status, articleId, chatRoomId);
        userSessionRepository.save(userId, webSocketUserSession);
        userSessionRepository.resetSessionTtl(userId);

        return webSocketUserSession;
    }

    public Long getSessionTtl(Integer userId) {
        return userSessionRepository.getSessionTtl(userId);
    }

    public void resetSessionTtl(Integer userId) {
        WebSocketUserSession webSocketUserSession = userSessionRepository.findUserSession(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.INVALID_WEBSOCKET_USER_SESSION));

        webSocketUserSession.updateLastActiveAt();
        userSessionRepository.save(userId, webSocketUserSession);
        userSessionRepository.resetSessionTtl(userId);
    }

    public void delete(Integer userId, String deviceId) {
        userSessionRepository.delete(userId);
    }
}
