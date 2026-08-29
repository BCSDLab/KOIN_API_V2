package in.koreatech.koin.domain.teamrecruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.teamrecruitment.model.enums.ChatRoomType;

public interface TeamRecruitmentChatRoomRepository extends JpaRepository<TeamRecruitmentChatRoom, Integer> {

    @Query("""
            SELECT cr FROM TeamRecruitmentChatRoom cr
            JOIN TeamRecruitmentChatRoomMember m1 ON m1.chatRoom = cr AND m1.user.id = :userId1
            JOIN TeamRecruitmentChatRoomMember m2 ON m2.chatRoom = cr AND m2.user.id = :userId2
            WHERE cr.recruitmentId = :recruitmentId AND cr.roomType = :roomType
            """)
    Optional<TeamRecruitmentChatRoom> findDirectChatRoom(
            @Param("recruitmentId") Integer recruitmentId,
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2,
            @Param("roomType") ChatRoomType roomType
    );
}
