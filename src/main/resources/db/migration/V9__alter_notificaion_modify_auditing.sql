alter table `notification`
    modify created_at TIMESTAMP not null comment '생성 일자';

alter table `notification`
    modify updated_at TIMESTAMP not null comment '수정 일자';

alter table `notification_subscribe`
    modify created_at TIMESTAMP not null comment '생성 일자';

alter table `notification_subscribe`
    modify updated_at TIMESTAMP not null comment '수정 일자';
