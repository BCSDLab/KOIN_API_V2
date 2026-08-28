package in.koreatech.koin.domain.teamrecruitment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentNotification;

public interface TeamRecruitmentNotificationRepository extends JpaRepository<TeamRecruitmentNotification, Integer> {

    Page<TeamRecruitmentNotification> findAllByRecipientIdOrderByCreatedAtDesc(Integer recipientId, Pageable pageable);
    long countByRecipientIdAndIsReadFalseAndIsDeletedFalse(Integer recipientId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TeamRecruitmentNotification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isDeleted = false")
    void updateIsReadByRecipientId(@Param("recipientId") Integer recipientId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TeamRecruitmentNotification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.id = :id AND n.isDeleted = false")
    void updateIsReadByRecipientIdAndNotificationId(
            @Param("recipientId") Integer recipientId,
            @Param("id") Integer notificationId
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TeamRecruitmentNotification n SET n.isDeleted = true WHERE n.recipient.id = :recipientId AND n.isDeleted = false")
    void updateIsDeletedByRecipientId(@Param("recipientId") Integer recipientId);

}
