package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentChatMessageRepository extends Repository<TeamRecruitmentChatMessage, Integer> {

    Optional<TeamRecruitmentChatMessage> findTopByChatRoom_IdOrderByIdDesc(Integer chatRoomId);

    TeamRecruitmentChatMessage save(TeamRecruitmentChatMessage message);

    @Query("""
        SELECT message
        FROM TeamRecruitmentChatMessage message
        WHERE message.chatRoom.id IN :chatRoomIds
        AND message.id = (
            SELECT MAX(latest.id)
            FROM TeamRecruitmentChatMessage latest
            WHERE latest.chatRoom.id = message.chatRoom.id
        )
        """)
    List<TeamRecruitmentChatMessage> findLatestByChatRoomIds(
        @Param("chatRoomIds") Collection<Integer> chatRoomIds
    );

    @Query("""
        SELECT message.chatRoom.id AS chatRoomId, COUNT(message.id) AS unreadMessageCount
        FROM TeamRecruitmentChatMessage message
        JOIN message.chatRoom.members member
        WHERE member.user.id = :userId
        AND message.sender.id <> :userId
        AND (member.lastReadMessageId IS NULL OR message.id > member.lastReadMessageId)
        GROUP BY message.chatRoom.id
        """)
    List<ChatRoomUnreadCount> countUnreadMessagesByUserId(@Param("userId") Integer userId);

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

    interface ChatRoomUnreadCount {

        Integer getChatRoomId();

        long getUnreadMessageCount();
    }
}
