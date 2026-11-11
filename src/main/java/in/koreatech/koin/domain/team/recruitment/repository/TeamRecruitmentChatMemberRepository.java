package in.koreatech.koin.domain.team.recruitment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;

public interface TeamRecruitmentChatMemberRepository extends Repository<TeamRecruitmentChatMember, Integer> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE TeamRecruitmentChatMember member SET member.lastReadMessageId = :messageId
        WHERE member.chatRoom.id = :roomId AND member.user.id = :userId
          AND (member.lastReadMessageId IS NULL OR member.lastReadMessageId < :messageId)
        """)
    int advanceReadCursor(@Param("roomId") Integer roomId, @Param("userId") Integer userId,
        @Param("messageId") Integer messageId);

    TeamRecruitmentChatMember save(TeamRecruitmentChatMember member);

    Optional<TeamRecruitmentChatMember> findByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    boolean existsByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    List<TeamRecruitmentChatMember> findAllByChatRoom_Id(Integer chatRoomId);

    @Query("""
        SELECT member
        FROM TeamRecruitmentChatMember member
        JOIN FETCH member.chatRoom chatRoom
        JOIN FETCH chatRoom.recruitment
        LEFT JOIN FETCH chatRoom.application
        WHERE member.user.id = :userId
        """)
    List<TeamRecruitmentChatMember> findAllByUserIdWithChatRoomAndRecruitment(@Param("userId") Integer userId);

    @Query("""
        SELECT member
        FROM TeamRecruitmentChatMember member
        JOIN FETCH member.user
        WHERE member.chatRoom.id IN :chatRoomIds
        """)
    List<TeamRecruitmentChatMember> findAllWithUsersByChatRoomIds(
        @Param("chatRoomIds") Collection<Integer> chatRoomIds
    );

    long countByChatRoom_Id(Integer chatRoomId);
}
