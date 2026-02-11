package in.koreatech.koin.domain.callvan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanNotification;

public interface CallvanNotificationRepository extends JpaRepository<CallvanNotification, Integer> {

    List<CallvanNotification> findAllByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanNotification n SET n.isRead = true WHERE n.recipient.id = :recipientId")
    void updateIsReadByRecipientId(@Param("recipientId") Integer recipientId);
}
