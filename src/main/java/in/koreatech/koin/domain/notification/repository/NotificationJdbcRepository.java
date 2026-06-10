package in.koreatech.koin.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.domain.notification.model.Notification;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepository {

    private static final int BATCH_SIZE = 1_000;

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO notification (
                app_path,
                scheme_uri,
                title,
                message,
                image_url,
                type,
                users_id,
                is_read,
                is_push_success,
                fcm_error_code,
                fcm_messaging_error_code,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
            sql,
            notifications,
            BATCH_SIZE,
            (preparedStatement, notification) -> {
                preparedStatement.setString(1, notification.getMobileAppPath().name());
                preparedStatement.setString(2, notification.getSchemeUri());
                preparedStatement.setString(3, notification.getTitle());
                preparedStatement.setString(4, notification.getMessage());
                preparedStatement.setString(5, notification.getImageUrl());
                preparedStatement.setString(6, notification.getType().toUpperCase());
                preparedStatement.setInt(7, notification.getUser().getId());
                preparedStatement.setBoolean(8, notification.isRead());
                preparedStatement.setObject(9, notification.getPushSuccess());
                preparedStatement.setString(10, notification.getFcmErrorCode());
                preparedStatement.setString(11, notification.getFcmMessagingErrorCode());
                preparedStatement.setObject(12, now);
                preparedStatement.setObject(13, now);
            }
        );
    }

}
