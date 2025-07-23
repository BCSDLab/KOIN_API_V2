package in.koreatech.koin.domain.community.lostitem.chatmessage.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class MessageProcessingException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "메시지 처리 과정에서 오류가 발생하였습니다.";

    public MessageProcessingException(String message) {
        super(message);
    }

    public MessageProcessingException(String message, String detail) {
        super(message, detail);
    }

    public static MessageProcessingException withDetail(String detail) {
        return new MessageProcessingException(DEFAULT_MESSAGE, detail);
    }
}
