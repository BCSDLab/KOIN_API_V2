package in.koreatech.koin.socket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.domain.session.service.implement.UserReader;
import in.koreatech.koin.socket.domain.session.model.UserSession;
import in.koreatech.koin.socket.domain.session.service.UserSessionService;
import in.koreatech.koin.socket.domain.session.model.UserSessionStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket의 STOMP 연결을 가로채 사용자 인증 및 웹소켓 세션 관리를 수행하는 인터셉터입니다.
 * <p>
 * WebSocket 연결이 이루어질 때 JWT(Json Web Token)를 사용하여 사용자의 인증을 수행하고,
 * 인증된 사용자에 대한 세션을 redis를 이용하여 관리합니다. 사용자의 ID와 디바이스 토큰을 기반으로 세션을 활성화하거나
 * 새로 생성합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *     <li>JWT 토큰을 통해 사용자의 인증을 수행합니다.</li>
 *     <li>인증된 사용자의 정보를 WebSocket 세션에 설정합니다.</li>
 *     <li>서버와 웹 소켓 연결을 처음 진행 하는 사용자라면 세션을 새로 생성합니다.</li>
 *     <li>웹 소켓 연결이 진행되었었던 사용자는 레디스에 저장된 세션의 상태 {@link UserSessionStatus#ACTIVE_APP}로 설정 합니다 </li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ConnectInterceptor implements ChannelInterceptor {

    private final UserReader userReader;
    private final UserSessionService userSessionService;
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                // JWT 토큰 추출 및 검증
                String accessToken = accessor.getFirstNativeHeader("Authorization");
                if (accessToken == null) {
                    throw new AuthenticationException("Authorization 헤더가 없거나 Bearer 토큰이 아닙니다.");
                }

                Integer userId = jwtProvider.getUserId(accessToken);

                User user = userReader.readUser(userId);
                UserPrincipal principal = UserPrincipal.of(user);

                // WebSocket 세션에 User Principal 설정
                accessor.setUser(principal);

                // 사용자 세션 활성화
                if (userSessionService.exists(principal.getUserId())) {
                    userSessionService.updateUserStatus(principal.getUserId(), UserSessionStatus.ACTIVE_APP);
                } else {
                    userSessionService.save(
                        principal.getUserId(),
                        UserSession.of(principal.getUserId(), principal.getDeviceToken())
                    );
                }
            } catch (Exception e) {
                // 예외 처리: 로그 출력 및 예외 전파
                System.out.println("WebSocket 연결 인증 실패: " + e.getMessage());
                throw e;
            }
        }

        return message;
    }
}
