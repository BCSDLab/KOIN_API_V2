package in.koreatech.koin.domain.chat.model;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public record ChatRoomKey(ChatDomainType domainType, Integer contextId, Integer chatRoomId) {

    private static final Pattern FORMAT =
        Pattern.compile("^([a-z]+(?:-[a-z]+)*)-([1-9][0-9]*)-([1-9][0-9]*)$");

    public ChatRoomKey {
        if (domainType == null || contextId == null || contextId < 1 || chatRoomId == null || chatRoomId < 1) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }

    public String encoded() {
        return domainType.getPrefix() + "-" + contextId + "-" + chatRoomId;
    }

    public static ChatRoomKey parse(String value) {
        Matcher matcher = FORMAT.matcher(value == null ? "" : value);
        if (!matcher.matches()) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        try {
            ChatDomainType domainType = Arrays.stream(ChatDomainType.values())
                .filter(type -> type.getPrefix().equals(matcher.group(1)))
                .findFirst().orElseThrow(() -> CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT));
            return new ChatRoomKey(domainType, Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)));
        } catch (NumberFormatException exception) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }
}
