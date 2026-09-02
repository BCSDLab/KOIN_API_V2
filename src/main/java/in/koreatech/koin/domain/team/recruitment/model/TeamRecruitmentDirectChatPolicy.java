package in.koreatech.koin.domain.team.recruitment.model;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import java.time.LocalDate;
import java.util.Objects;

public final class TeamRecruitmentDirectChatPolicy {

    private TeamRecruitmentDirectChatPolicy() {
    }

    public static boolean canOpenDirectChat(
        TeamRecruitmentApplicationStatus applicationStatus,
        boolean hasExistingDirectChat,
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom teamChatRoom,
        LocalDate today
    ) {
        if (applicationStatus != ACCEPTED) {
            return false;
        }
        if (hasExistingDirectChat) {
            return true;
        }
        if (recruitment == null || recruitment.isDeleted()) {
            return false;
        }
        Objects.requireNonNull(today, "today must not be null");
        if (recruitment.isRecruiting()) {
            return recruitment.getDeadlineDate() == null
                || !today.isAfter(recruitment.getDeadlineDate());
        }
        return recruitment.getStatus() == CLOSED
            && teamChatRoom != null
            && teamChatRoom.getRoomType() == TEAM
            && teamChatRoom.isActive();
    }
}
