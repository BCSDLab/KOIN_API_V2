UPDATE coop
    JOIN users ON coop.user_id = users.id
    SET coop.coop_id = users.email;
