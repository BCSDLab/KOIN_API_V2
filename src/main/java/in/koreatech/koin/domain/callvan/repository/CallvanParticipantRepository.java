package in.koreatech.koin.domain.callvan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;

public interface CallvanParticipantRepository extends Repository<CallvanParticipant, Integer> {

    @Query("""
        SELECT participant FROM CallvanParticipant participant
        JOIN FETCH participant.post post
        JOIN FETCH post.chatRoom room
        WHERE participant.member.id = :memberId AND participant.isDeleted = false
            AND post.isDeleted = false AND room.isDeleted = false
        """)
    List<CallvanParticipant> findActiveWithChatRoomByMemberId(@Param("memberId") Integer memberId);

    @Query("""
        SELECT participant FROM CallvanParticipant participant
        JOIN FETCH participant.post post
        JOIN FETCH post.chatRoom room
        WHERE participant.member.id = :memberId AND post.id = :postId
            AND participant.isDeleted = false AND post.isDeleted = false AND room.isDeleted = false
        """)
    Optional<CallvanParticipant> findActiveWithChatRoomByPostIdAndMemberId(
        @Param("postId") Integer postId, @Param("memberId") Integer memberId);

    @Query("""
        SELECT participant.member.id AS memberId, participant.lastReadMessageId AS lastReadMessageId
        FROM CallvanParticipant participant
        WHERE participant.post.id = :postId AND participant.isDeleted = false
            AND participant.member.isDeleted = false
        """)
    List<ReadCursor> findActiveReadCursorsByPostId(@Param("postId") Integer postId);

    interface ReadCursor {
        Integer getMemberId();
        Integer getLastReadMessageId();
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE CallvanParticipant participant SET participant.lastReadMessageId = :messageId
        WHERE participant.post.id = :postId AND participant.member.id = :memberId
            AND participant.isDeleted = false AND participant.lastReadMessageId < :messageId
        """)
    int advanceReadCursor(@Param("postId") Integer postId, @Param("memberId") Integer memberId,
        @Param("messageId") Integer messageId);

    @Query("""
        SELECT COUNT(participant) FROM CallvanParticipant participant
        WHERE participant.post.id = :postId AND participant.isDeleted = false
            AND participant.member.isDeleted = false
            AND (:senderId IS NULL OR participant.member.id <> :senderId)
            AND participant.lastReadMessageId < :messageId
        """)
    int countUnreadParticipants(@Param("postId") Integer postId, @Param("senderId") Integer senderId,
        @Param("messageId") Integer messageId);

    CallvanParticipant save(CallvanParticipant callvanParticipant);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Integer postId, Integer memberId);

    boolean existsByPostIdAndMemberIdAndIsDeletedTrue(Integer postId, Integer memberId);

    Optional<CallvanParticipant> findByPostIdAndMemberId(Integer postId, Integer memberId);

    void delete(CallvanParticipant callvanParticipant);

    List<CallvanParticipant> findAllByMemberIdAndPostIdInAndIsDeletedFalse(Integer memberId, List<Integer> postIds);
}
