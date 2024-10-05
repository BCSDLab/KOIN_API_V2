insert into `coop_shop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
values ('24-2학기', '2024-09-02', '2024-12-20', 1);

update `coop_shop` set `semester_id` = 1, `name` = 'CAFETERIA' where `id` = 1;

update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 1;
update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 2;
update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 3;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 4;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 5;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 6;
