package in.koreatech.koin.domain.chat.dto;

import java.util.Comparator;
import java.util.List;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public record ChatMessageQuery(Long afterMessageId, Long beforeMessageId, int limit) {

    public ChatMessageQuery {
        if ((afterMessageId != null && beforeMessageId != null)
            || (afterMessageId != null && afterMessageId < 1)
            || (beforeMessageId != null && beforeMessageId < 1)
            || limit < 1 || limit > 200) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }

    public Integer afterAsInteger() {
        return asInteger(afterMessageId);
    }

    public Integer beforeAsInteger() {
        return asInteger(beforeMessageId);
    }

    private Integer asInteger(Long value) {
        if (value != null && value > Integer.MAX_VALUE) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        return value == null ? null : value.intValue();
    }

    public List<ChatMessageResponse> select(List<ChatMessageResponse> messages) {
        List<ChatMessageResponse> selected = messages.stream()
            .filter(message -> afterMessageId == null || Long.parseLong(message.messageId()) > afterMessageId)
            .filter(message -> beforeMessageId == null || Long.parseLong(message.messageId()) < beforeMessageId)
            .sorted(Comparator.comparingLong(message -> Long.parseLong(message.messageId())))
            .toList();
        int from = afterMessageId == null ? Math.max(0, selected.size() - limit) : 0;
        return selected.subList(from, Math.min(selected.size(), from + limit));
    }
}
