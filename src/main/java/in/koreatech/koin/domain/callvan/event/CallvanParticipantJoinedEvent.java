package in.koreatech.koin.domain.callvan.event;

public record CallvanParticipantJoinedEvent(
    Integer callvanPostId,
    Integer joinUserId,
    String joinUserNickname
) {

}
