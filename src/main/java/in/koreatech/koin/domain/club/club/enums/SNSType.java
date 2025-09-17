package in.koreatech.koin.domain.club.club.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SNSType {
    INSTAGRAM("인스타그램"),
    PHONE_NUMBER("전화 번호"),
    GOOGLE_FORM("구글 폼"),
    OPEN_CHAT("오픈 채팅"),
    ;

    private final String displayName;
}
