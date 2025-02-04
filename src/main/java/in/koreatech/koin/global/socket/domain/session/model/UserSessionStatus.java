package in.koreatech.koin.global.socket.domain.session.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSessionStatus {

    ACTIVE_APP("1", "앱 활성화"),
    ACTIVE_CHAT_ROOM("2", "채팅방 뷰"),
    INACTIVE("3", "비활성화");

    private final String code;
    private final String type;
}
