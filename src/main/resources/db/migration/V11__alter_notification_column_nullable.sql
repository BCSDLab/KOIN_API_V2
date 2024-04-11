alter table `notification`
    modify app_path VARCHAR(255) NULL comment '앱 url';

alter table `notification`
    modify title VARCHAR(255) NULL comment '제목';

alter table `notification`
    modify message VARCHAR(255) NULL comment '메시지 내용';

alter table `notification`
    modify image_url VARCHAR(255) NULL comment '이미지 url';

alter table `notification`
    modify type VARCHAR(255) NULL comment '알림 타입';
