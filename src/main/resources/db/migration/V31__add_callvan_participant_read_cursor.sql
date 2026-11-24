ALTER TABLE callvan_participant
    ADD COLUMN last_read_message_id INT NOT NULL DEFAULT 0;

-- Existing participants start at the room's latest message, avoiding historical unread alerts.
UPDATE callvan_participant participant
LEFT JOIN callvan_chat_room room ON room.callvan_post_id = participant.post_id
LEFT JOIN (
    SELECT callvan_chat_room_id, MAX(id) AS last_message_id
    FROM callvan_chat_message
    WHERE is_deleted = 0
    GROUP BY callvan_chat_room_id
) latest ON latest.callvan_chat_room_id = room.id
SET participant.last_read_message_id = COALESCE(latest.last_message_id, 0);
