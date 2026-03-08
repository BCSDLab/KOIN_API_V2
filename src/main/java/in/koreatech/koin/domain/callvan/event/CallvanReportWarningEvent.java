package in.koreatech.koin.domain.callvan.event;

public record CallvanReportWarningEvent(
    Integer reportedUserId,
    Integer callvanPostId
) {
}
