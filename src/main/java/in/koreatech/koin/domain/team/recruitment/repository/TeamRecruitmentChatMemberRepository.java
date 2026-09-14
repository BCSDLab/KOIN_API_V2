package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentChatMemberRepository extends Repository<TeamRecruitmentChatMember, Integer> {

    TeamRecruitmentChatMember save(TeamRecruitmentChatMember member);

    Optional<TeamRecruitmentChatMember> findByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    boolean existsByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    List<TeamRecruitmentChatMember> findAllByChatRoom_Id(Integer chatRoomId);

    @Query("""
        SELECT member
        FROM TeamRecruitmentChatMember member
        JOIN FETCH member.chatRoom chatRoom
        JOIN FETCH chatRoom.recruitment
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
