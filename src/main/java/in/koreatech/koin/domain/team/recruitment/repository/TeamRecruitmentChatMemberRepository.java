package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentChatMemberRepository extends Repository<TeamRecruitmentChatMember, Integer> {

    TeamRecruitmentChatMember save(TeamRecruitmentChatMember member);

    Optional<TeamRecruitmentChatMember> findByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    boolean existsByChatRoom_IdAndUser_Id(Integer chatRoomId, Integer userId);

    List<TeamRecruitmentChatMember> findAllByChatRoom_Id(Integer chatRoomId);

    long countByChatRoom_Id(Integer chatRoomId);
}
