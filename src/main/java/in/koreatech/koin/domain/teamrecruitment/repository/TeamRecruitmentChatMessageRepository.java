package in.koreatech.koin.domain.teamrecruitment.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatMessage;

public interface TeamRecruitmentChatMessageRepository extends JpaRepository<TeamRecruitmentChatMessage, Integer> {

    // 최신 메시지 조회 (초기 로드 - 내림차순, 이후 역순 처리)
    List<TeamRecruitmentChatMessage> findByChatRoomIdOrderByIdDesc(Integer chatRoomId, Pageable pageable);

    // afterMessageId 이후 메시지 (새 메시지 폴링)
    List<TeamRecruitmentChatMessage> findByChatRoomIdAndIdGreaterThanOrderByIdAsc(Integer chatRoomId, Integer afterMessageId, Pageable pageable);

    // beforeMessageId 이전 메시지 (과거 메시지 조회)
    List<TeamRecruitmentChatMessage> findByChatRoomIdAndIdLessThanOrderByIdDesc(Integer chatRoomId, Integer beforeMessageId, Pageable pageable);
}
