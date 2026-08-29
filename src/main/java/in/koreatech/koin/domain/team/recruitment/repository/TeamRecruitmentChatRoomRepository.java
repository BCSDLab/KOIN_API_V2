package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface TeamRecruitmentChatRoomRepository extends Repository<TeamRecruitmentChatRoom, Integer> {

    TeamRecruitmentChatRoom save(TeamRecruitmentChatRoom chatRoom);

    Optional<TeamRecruitmentChatRoom> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT chatRoom
        FROM TeamRecruitmentChatRoom chatRoom
        WHERE chatRoom.id = :id
        """)
    Optional<TeamRecruitmentChatRoom> findByIdWithLock(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT chatRoom
        FROM TeamRecruitmentChatRoom chatRoom
        WHERE chatRoom.id = :chatRoomId
        AND chatRoom.recruitment.id = :recruitmentId
        """)
    Optional<TeamRecruitmentChatRoom> findByIdAndRecruitmentIdWithLock(
        @Param("chatRoomId") Integer chatRoomId,
        @Param("recruitmentId") Integer recruitmentId
    );

    Optional<TeamRecruitmentChatRoom> findByRecruitment_IdAndRoomScopeKey(
        Integer recruitmentId,
        String roomScopeKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT chatRoom
        FROM TeamRecruitmentChatRoom chatRoom
        WHERE chatRoom.recruitment.id = :recruitmentId
        AND chatRoom.roomScopeKey = :roomScopeKey
        """)
    Optional<TeamRecruitmentChatRoom> findByRecruitmentIdAndRoomScopeKeyWithLock(
        @Param("recruitmentId") Integer recruitmentId,
        @Param("roomScopeKey") String roomScopeKey
    );

    Optional<TeamRecruitmentChatRoom> findByRecruitment_IdAndApplication_IdAndRoomType(
        Integer recruitmentId,
        Integer applicationId,
        TeamRecruitmentChatRoomType roomType
    );

    List<TeamRecruitmentChatRoom> findAllByRecruitment_IdInAndRoomScopeKeyAndRoomType(
        Collection<Integer> recruitmentIds,
        String roomScopeKey,
        TeamRecruitmentChatRoomType roomType
    );

    List<TeamRecruitmentChatRoom> findAllByApplication_IdInAndRoomType(
        Collection<Integer> applicationIds,
        TeamRecruitmentChatRoomType roomType
    );

    List<TeamRecruitmentChatRoom> findAllByRecruitment_Id(Integer recruitmentId);
}
