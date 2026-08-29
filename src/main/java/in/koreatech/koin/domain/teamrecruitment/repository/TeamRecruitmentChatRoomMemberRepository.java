package in.koreatech.koin.domain.teamrecruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatRoomMember;

public interface TeamRecruitmentChatRoomMemberRepository extends JpaRepository<TeamRecruitmentChatRoomMember, Integer> {

    List<TeamRecruitmentChatRoomMember> findAllByChatRoomId(Integer chatRoomId);

    Optional<TeamRecruitmentChatRoomMember> findByChatRoomIdAndUserId(Integer chatRoomId, Integer userId);

    boolean existsByChatRoomIdAndUserId(Integer chatRoomId, Integer userId);

    int countByChatRoomId(Integer chatRoomId);
}
