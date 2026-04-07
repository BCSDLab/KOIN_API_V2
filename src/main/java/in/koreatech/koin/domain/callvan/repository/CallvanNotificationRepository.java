package in.koreatech.koin.domain.callvan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanNotification;

public interface CallvanNotificationRepository extends JpaRepository<CallvanNotification, Integer> {

    List<CallvanNotification> findAllByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    @Query("""
        SELECT DISTINCT n
        FROM CallvanNotification n
        JOIN FETCH n.recipient
        LEFT JOIN FETCH n.post
        LEFT JOIN FETCH n.chatRoom
        WHERE n.id IN :notificationIds
        AND n.isDeleted = false
        """)
    List<CallvanNotification> findAllByIdInWithRelations(@Param("notificationIds") List<Integer> notificationIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanNotification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isDeleted = false")
    void updateIsReadByRecipientId(@Param("recipientId") Integer recipientId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanNotification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.id = :id AND n.isDeleted = false")
    void updateIsReadByRecipientIdAndNotificationId(
        @Param("recipientId") Integer recipientId,
        @Param("id") Integer notificationId
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanNotification n SET n.isDeleted = true WHERE n.recipient.id = :recipientId AND n.isDeleted = false")
    void updateIsDeletedByRecipientId(@Param("recipientId") Integer recipientId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanNotification n SET n.isDeleted = true WHERE n.recipient.id = :recipientId AND n.id = :id AND n.isDeleted = false")
    void updateIsDeletedByRecipientIdAndNotificationId(
        @Param("recipientId") Integer recipientId,
        @Param("id") Integer notificationId
    );
}
