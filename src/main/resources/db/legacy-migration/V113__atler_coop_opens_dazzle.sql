UPDATE coop_opens AS co
    JOIN coop_shop AS cs ON co.coop_shop_id = cs.id
    JOIN coop_names AS cn ON cs.coop_name_id = cn.id
    SET co.day_of_week = 'WEEKDAYS'
WHERE cn.name = '대즐' AND co.open_time = '08:30';