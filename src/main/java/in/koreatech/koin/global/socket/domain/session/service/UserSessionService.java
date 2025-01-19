package in.koreatech.koin.global.socket.domain.session.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.socket.domain.session.model.UserSession;
import in.koreatech.koin.global.socket.domain.session.repository.UserSessionRedisRepository;
import in.koreatech.koin.global.socket.domain.session.model.UserSessionStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRedisRepository userSessionRepository;

    public void save(Integer userId, String deviceToken, UserSession value) {
        userSessionRepository.save(userId, deviceToken, value);
    }

    public Optional<UserSession> read(Integer userId, String deviceId) {
        return userSessionRepository.findUserSession(userId, deviceId);
    }

    public boolean isExists(Integer userId, String deviceId) {
        return userSessionRepository.exists(userId, deviceId);
    }

    public UserSession updateUserStatus(Integer userId, String deviceToken, UserSessionStatus status) {
        return updateUserStatus(userId, deviceToken, -1L, -1L, status);
    }

    public UserSession updateUserStatus(Integer userId, String deviceToken, Long articleId, Long chatRoomId) {
        return updateUserStatus(userId, deviceToken, articleId, chatRoomId, UserSessionStatus.ACTIVE_CHAT_ROOM);
    }

    private UserSession updateUserStatus(Integer userId, String deviceToken, Long articleId, Long chatRoomId, UserSessionStatus status) {
        UserSession userSession = userSessionRepository.findUserSession(userId, deviceToken)
            .orElseThrow(() -> new KoinIllegalArgumentException("웹소켓 사용자 세션 탐색 실패"));

        userSession.updateStatus(status, articleId, chatRoomId);
        userSessionRepository.save(userId, deviceToken, userSession);
        userSessionRepository.resetSessionTtl(userId, deviceToken);

        return userSession;
    }

    public Long getSessionTtl(Integer userId, String deviceToken) {
        return userSessionRepository.getSessionTtl(userId, deviceToken);
    }

    public void resetSessionTtl(Integer userId, String deviceToken) {
        UserSession userSession = userSessionRepository.findUserSession(userId, deviceToken)
            .orElseThrow(() -> new KoinIllegalArgumentException("웹소켓 사용자 세션 탐색 실패"));

        userSession.updateLastActiveAt();
        userSessionRepository.save(userId, deviceToken, userSession);
        userSessionRepository.resetSessionTtl(userId, deviceToken);
    }

    public void delete(Integer userId, String deviceId) {
        userSessionRepository.delete(userId, deviceId);
    }
}
