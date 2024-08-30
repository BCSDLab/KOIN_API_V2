UPDATE koreatech_articles a
    JOIN boards b ON a.board_id = b.id
    SET a.is_notice = b.is_notice;
