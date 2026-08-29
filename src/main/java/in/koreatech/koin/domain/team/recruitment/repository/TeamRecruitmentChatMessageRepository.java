package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentChatMessageRepository extends Repository<TeamRecruitmentChatMessage, Integer> {

    Optional<TeamRecruitmentChatMessage> findTopByChatRoom_IdOrderByIdDesc(Integer chatRoomId);

    TeamRecruitmentChatMessage save(TeamRecruitmentChatMessage message);

    /** Returns the initial page in ascending message-id order. */
    List<TeamRecruitmentChatMessage> findAllByChatRoom_IdOrderByIdAsc(
        Integer chatRoomId,
        Pageable pageable
    );

    /** Returns messages newer than the polling cursor. */
    List<TeamRecruitmentChatMessage> findAllByChatRoom_IdAndIdGreaterThanOrderByIdAsc(
        Integer chatRoomId,
        Integer afterMessageId,
        Pageable pageable
    );

    /** Returns the closest messages before the cursor first. */
    List<TeamRecruitmentChatMessage> findAllByChatRoom_IdAndIdLessThanOrderByIdDesc(
        Integer chatRoomId,
        Integer beforeMessageId,
        Pageable pageable
    );
}
