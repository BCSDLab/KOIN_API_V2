package in.koreatech.koin.socket.domain.session.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSessionStatus {

    ACTIVE_APP("1", "웹 소켓 연결"),
    ACTIVE_CHAT_ROOM("2", "채팅방 뷰"),
    INACTIVE("3", "웹 소켓 연결 해제"),
    ACTIVE_CHAT_ROOM_LIST("4", "채팅방 리스트");

    private final String code;
    private final String type;
}
