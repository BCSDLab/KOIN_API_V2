package in.koreatech.koin.socket.domain.session.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.socket.domain.session.model.UserSession;
import in.koreatech.koin.socket.domain.session.repository.UserSessionRedisRepository;
import in.koreatech.koin.socket.domain.session.model.UserSessionStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRedisRepository userSessionRepository;

    public void save(Integer userId, UserSession value) {
        userSessionRepository.save(userId, value);
    }

    public Optional<UserSession> read(Integer userId) {
        return userSessionRepository.findUserSession(userId);
    }

    public boolean isExists(Integer userId) {
        return userSessionRepository.exists(userId);
    }

    public UserSession updateUserStatus(Integer userId, UserSessionStatus status) {
        return updateUserStatus(userId, -1L, -1L, status);
    }

    public UserSession updateUserStatus(Integer userId, Long articleId, Long chatRoomId) {
        return updateUserStatus(userId, articleId, chatRoomId, UserSessionStatus.ACTIVE_CHAT_ROOM);
    }

    private UserSession updateUserStatus(Integer userId, Long articleId, Long chatRoomId, UserSessionStatus status) {
        UserSession userSession = userSessionRepository.findUserSession(userId)
            .orElseThrow(() -> new KoinIllegalArgumentException("웹소켓 사용자 세션 탐색 실패"));

        userSession.updateStatus(status, articleId, chatRoomId);
        userSessionRepository.save(userId, userSession);
        userSessionRepository.resetSessionTtl(userId);

        return userSession;
    }

    public Long getSessionTtl(Integer userId) {
        return userSessionRepository.getSessionTtl(userId);
    }

    public void resetSessionTtl(Integer userId) {
        UserSession userSession = userSessionRepository.findUserSession(userId)
            .orElseThrow(() -> new KoinIllegalArgumentException("웹소켓 사용자 세션 탐색 실패"));

        userSession.updateLastActiveAt();
        userSessionRepository.save(userId, userSession);
        userSessionRepository.resetSessionTtl(userId);
    }

    public void delete(Integer userId, String deviceId) {
        userSessionRepository.delete(userId);
    }
}
