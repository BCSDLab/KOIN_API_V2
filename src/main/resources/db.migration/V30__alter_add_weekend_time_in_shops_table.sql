ALTER TABLE shops ADD weekend_close_time varchar(10) DEFAULT NULL after close_time;
ALTER TABLE shops ADD weekend_open_time varchar(10) DEFAULT NULL after close_time;

