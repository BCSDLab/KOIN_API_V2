create table if not exists activities
(
    id          int unsigned auto_increment comment 'activities 고유 id'
        primary key,
    title       varchar(255)                         not null comment '활동명',
    description text                                 null comment '활동 설명',
    image_urls  text                                 null comment '이미지 링크',
    date        date                                 not null comment '활동 일자',
    is_deleted  tinyint(1) default 0                 not null comment '삭제 여부',
    created_at  timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists admins
(
    id              int unsigned auto_increment comment 'admins 고유 id'
        primary key,
    user_id         int unsigned                         not null comment '해당 user 고유 id',
    grant_user      tinyint(1) default 0                 not null comment 'user 수정 권한',
    grant_callvan   tinyint(1) default 0                 not null comment '콜벤 수정 권한',
    grant_land      tinyint(1) default 0                 not null comment '복덕방 수정 권한',
    grant_community tinyint(1) default 0                 not null comment '커뮤니티 수정 권한',
    grant_shop      tinyint(1) default 0                 not null comment '상점 수정 권한',
    grant_version   tinyint(1) default 0                 not null comment '버전 수정 권한',
    is_deleted      tinyint(1) default 0                 not null comment '삭제 여부',
    created_at      timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at      timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    grant_market    tinyint(1) default 0                 not null comment '거래 수정 권한',
    grant_circle    tinyint(1) default 0                 not null comment '동아리 수정 권한',
    grant_lost      tinyint(1) default 0                 not null comment '분실물 수정 권한',
    grant_survey    tinyint(1) default 0                 not null comment '조사 수정 구너한',
    grant_bcsdlab   tinyint(1) default 0                 not null comment 'bcsdlab 홈페이지 수정 권한',
    grant_event     tinyint(1) default 0                 not null comment '이벤트 수정 권한',
    constraint admins_user_id_unique
        unique (user_id)
)
    collate = utf8_unicode_ci;

create table if not exists article_view_logs
(
    id         int unsigned auto_increment comment 'article view logs 고유 id'
        primary key,
    article_id int unsigned                        not null comment 'article 고유 id',
    user_id    int unsigned                        null comment '본 사람 user 고유 id',
    expired_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '만료 시간',
    ip         varchar(45)                         not null comment 'IP 주소',
    constraint article_view_logs_article_id_user_id_unique
        unique (article_id, user_id)
)
    collate = utf8_unicode_ci;

create table if not exists articles
(
    id                int unsigned auto_increment comment 'articlees 고유 id'
        primary key,
    board_id          int unsigned                               not null comment '게시판의 고유 id',
    title             varchar(255)                               not null comment '제목',
    content           mediumtext                                 not null comment '내용',
    user_id           int unsigned                               not null comment '작성자 user 고유 id',
    nickname          varchar(50)                                not null comment '작성자 user 닉네임',
    hit               int unsigned     default 0                 not null comment '조회수',
    ip                varchar(45)                                not null comment 'IP 주소',
    is_solved         tinyint(1)       default 0                 not null comment '해결 여부(문의 게시판에 사용)',
    is_deleted        tinyint(1)       default 0                 not null comment '삭제 여부',
    created_at        timestamp        default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at        timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    comment_count     tinyint unsigned default 0                 not null comment '전체 댓글 수',
    meta              text                                       null comment '등록일자 / 공지사항 주소 등 정보를 String으로 저장',
    is_notice         tinyint(1)       default 0                 not null comment '공지사항인지 여부',
    notice_article_id int unsigned                               null comment '공지사항 고유 id',
    constraint notice_article_id_UNIQUE
        unique (notice_article_id)
)
    collate = utf8_unicode_ci;

create table if not exists boards
(
    id            int unsigned auto_increment comment 'board 고유 id'
        primary key,
    tag           varchar(10)                            not null comment '게시판 태그',
    name          varchar(50)                            not null comment '게시판 이름',
    is_anonymous  tinyint(1)   default 0                 not null comment '익명 닉네임을 사용하는지 여부',
    article_count int unsigned default 0                 not null comment '게시글 개수',
    is_deleted    tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at    timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at    timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    is_notice     tinyint(1)   default 0                 not null comment '공지사항인지 여부',
    parent_id     int unsigned                           null,
    seq           int unsigned default 0                 not null,
    constraint boards_tag_unique
        unique (tag)
)
    collate = utf8_unicode_ci;

create table if not exists calendar_universities
(
    id           int unsigned auto_increment
        primary key,
    year         varchar(10)                          not null,
    start_month  varchar(10)                          not null,
    end_month    varchar(10)                          not null,
    start_day    varchar(10)                          not null,
    end_day      varchar(10)                          not null,
    schedule     varchar(255)                         not null,
    seq          int unsigned                         not null,
    is_continued tinyint(1) default 0                 not null,
    created_at   timestamp  default CURRENT_TIMESTAMP not null,
    updated_at   timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint ux_year_seq
        unique (year, seq)
)
    collate = utf8_unicode_ci;

create table if not exists callvan_companies
(
    id         int unsigned auto_increment comment 'callvan companies 고유 id'
        primary key,
    name       varchar(100)                           not null comment '콜벤 회사 이름',
    phone      varchar(100)                           not null comment '콜벤 회사 전화번호',
    pay_card   tinyint(1)   default 0                 not null comment '카드 여부(0:미사용 / 1:사용)',
    pay_bank   tinyint(1)   default 0                 not null comment '계좌이체 여부(0: 미사용 / 1:사용)',
    hit        int unsigned default 0                 not null comment '전화 횟수',
    is_deleted tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint callvan_companies_name_unique
        unique (name)
)
    collate = utf8_unicode_ci;

create table if not exists callvan_participants
(
    id         int unsigned auto_increment comment 'callvan participants 고유 id'
        primary key,
    room_id    int unsigned                         not null comment 'room 고유 id',
    user_id    int unsigned                         not null comment 'user 고유 id',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists callvan_rooms
(
    id                 int unsigned auto_increment comment 'callvan rooms 고유 id'
        primary key,
    user_id            int unsigned                           not null comment '방장을 맡은 user 고유 id',
    departure_place    varchar(100)                           not null comment '출발 장소',
    departure_datetime timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '출발 시간',
    arrival_place      varchar(100)                           not null comment '도착 장소',
    maximum_people     int unsigned default 2                 not null comment '최대 인원',
    current_people     int unsigned default 1                 not null comment '현재 인원',
    is_deleted         tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at         timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at         timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists circles
(
    id                 int unsigned auto_increment comment 'circles 고유 id'
        primary key,
    category           varchar(10)                          not null comment '동아리 카테고리',
    name               varchar(50)                          not null comment '동아리 이름',
    line_description   varchar(255)                         null comment '한 줄 설명',
    logo_url           text                                 null comment '로고 이미지 링크',
    description        text                                 null comment '세부 사항',
    link_urls          text                                 null comment '외부 링크',
    background_img_url text                                 null comment '배경 이미지 링크',
    is_deleted         tinyint(1) default 0                 not null comment '삭제 여부',
    professor          varchar(255)                         null comment '담당 교수',
    location           varchar(255)                         null comment '동아리방 위치',
    major_business     varchar(255)                         null comment '주요 사업',
    introduce_url      varchar(255)                         null comment '동아리 소개 홈페이지 url',
    created_at         timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at         timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists comments
(
    id         int unsigned auto_increment comment 'comment 고유 id'
        primary key,
    article_id int unsigned                         not null comment '게시글 고유 id',
    content    text                                 not null comment '내용',
    user_id    int unsigned                         not null comment '답글 user 고유 id',
    nickname   varchar(50)                          not null comment '답글 user 닉네임',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists courses
(
    id         int unsigned auto_increment
        primary key,
    region     varchar(10)          not null,
    bus_type   varchar(15)          not null,
    is_deleted tinyint(1) default 0 not null
)
    collate = utf8mb4_bin;

create table if not exists dept_infos
(
    name            varchar(45)          not null
        primary key,
    curriculum_link varchar(255)         not null,
    is_deleted      tinyint(1) default 0 not null,
    constraint dept_name_UNIQUE
        unique (name)
)
    collate = utf8mb4_bin;

create table if not exists dept_nums
(
    dept_name varchar(45) not null,
    dept_num  varchar(5)  not null,
    primary key (dept_name, dept_num),
    constraint fk_dept_name
        foreign key (dept_name) references dept_infos (name)
            on update cascade
)
    collate = utf8mb4_bin;

create index idx_dept_num
    on dept_nums (dept_num);

create table if not exists dining_menus
(
    id         int unsigned auto_increment comment 'dining menus 고유 id'
        primary key,
    date       date                                not null comment '일자',
    type       varchar(9)                          not null comment '식사 유형(아침 , 점심, 저녁)',
    place      varchar(9)                          not null comment '종류(양식, 한식..)',
    price_card int unsigned                        null comment '카드 금액',
    price_cash int unsigned                        null comment '현금 금액',
    kcal       int unsigned                        null comment '칼로리',
    menu       text                                not null comment '메뉴',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint ux_date_type_place
        unique (date, type, place)
)
    collate = utf8_unicode_ci;

create table if not exists event_articles
(
    id            int unsigned auto_increment comment 'event articles 고유 id'
        primary key,
    shop_id       int unsigned                         not null comment 'Shop(가게) 고유 id',
    title         varchar(255)                         not null comment '제목',
    event_title   varchar(50)                          not null comment '홍보 문구',
    content       text                                 not null comment '내용',
    user_id       int                                  not null comment 'user(작성자) 고유 id',
    nickname      varchar(50)                          not null comment '작성자 user 닉네임',
    thumbnail     varchar(255)                         null comment '썸네일 이미지',
    hit           int        default 0                 not null comment '조회수',
    ip            varchar(45)                          not null comment 'IP 주소',
    start_date    date                                 not null comment '행사 시작일',
    end_date      date                                 not null comment '행사 마감일',
    comment_count tinyint(1) default 0                 not null comment '전체 댓글수',
    is_deleted    tinyint(1) default 0                 not null comment '삭제 여부',
    created_at    timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at    timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint pk
        unique (id)
)
    collate = utf8_bin;

create index idx_is_deleted
    on event_articles (is_deleted);

create index idx_timestamp
    on event_articles (created_at);

create table if not exists event_articles_view_logs
(
    id                int unsigned auto_increment comment 'event articles view logs 고유 id'
        primary key,
    event_articles_id int unsigned not null comment 'event articles 고유 id',
    user_id           int unsigned null comment '게시물을 본 user 고유 id',
    expired_at        timestamp    null comment '만료 일자',
    ip                varchar(45)  not null comment 'IP 주소',
    constraint idx_unique
        unique (event_articles_id, user_id)
)
    collate = utf8_bin;

create table if not exists event_comments
(
    id         int unsigned auto_increment comment 'event comments 고유 id'
        primary key,
    article_id int unsigned                         not null comment 'article(게시글) 고유 id',
    content    text                                 not null comment '내용',
    user_id    int unsigned                         not null comment '답글 user 고유 id',
    nickname   varchar(50)                          not null comment '답글 user 닉네임',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists failed_jobs
(
    id         bigint unsigned auto_increment
        primary key,
    connection text                                not null,
    queue      text                                not null,
    payload    longtext                            not null,
    exception  longtext                            not null,
    failed_at  timestamp default CURRENT_TIMESTAMP not null
)
    collate = utf8_unicode_ci;

create table if not exists faqs
(
    id         int unsigned auto_increment comment 'faqs 고유 id'
        primary key,
    question   varchar(255)                           not null comment '질문',
    answer     text                                   not null comment '답변',
    is_deleted tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    circle_id  int unsigned default 0                 not null comment '관련 circle(동아리) id'
)
    collate = utf8_unicode_ci;

create table if not exists flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table if not exists holidays
(
    id   int unsigned auto_increment
        primary key,
    name varchar(45) not null,
    date date        not null
)
    collate = utf8_bin;

create table if not exists integrated_assessments
(
    id           int unsigned auto_increment
        primary key,
    service_type varchar(255)                           not null,
    evaluated_id int unsigned                           not null,
    score_one    int unsigned default 0                 not null,
    score_two    int unsigned default 0                 not null,
    score_three  int unsigned default 0                 not null,
    score_four   int unsigned default 0                 not null,
    score_five   int unsigned default 0                 not null,
    score_six    int unsigned default 0                 not null,
    score_seven  int unsigned default 0                 not null,
    score_eight  int unsigned default 0                 not null,
    score_nine   int unsigned default 0                 not null,
    score_ten    int unsigned default 0                 not null,
    is_deleted   tinyint(1)   default 0                 not null,
    created_at   timestamp    default CURRENT_TIMESTAMP not null,
    updated_at   timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    collate = utf8_unicode_ci;

create table if not exists item_comments
(
    id         int unsigned auto_increment comment 'item comments 고유 id'
        primary key,
    item_id    int unsigned                         not null comment 'item 고유 id',
    content    text                                 not null comment '내용',
    user_id    int unsigned                         not null comment '답글 user 고유 id',
    nickname   varchar(50)                          not null comment '답글 user 닉네임',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists item_view_logs
(
    id         int unsigned auto_increment comment 'item view logs 고유 id'
        primary key,
    item_id    int unsigned not null comment 'item 고유 id',
    user_id    int unsigned null comment 'user(본 사람) 고유 id',
    expired_at timestamp    null comment '만료 시간',
    ip         varchar(45)  not null comment 'IP 주소',
    constraint item_view_logs_item_id_user_id_unique
        unique (item_id, user_id)
)
    collate = utf8_unicode_ci;

create table if not exists items
(
    id            int unsigned auto_increment comment 'items 고유 id'
        primary key,
    type          int unsigned                           not null comment '서비스 타입(0:팝니다 / 1:삽니다)',
    title         varchar(255)                           not null comment '제목',
    content       text                                   null comment '내용',
    user_id       int unsigned                           not null comment '작성자 user 고유 id',
    nickname      varchar(50)                            not null comment '작성자 user 닉네임',
    state         int unsigned default 0                 not null comment '상태 정보(0:판매중 / 1:판매완료 / 2:판매중지)',
    price         int unsigned default 0                 not null comment '가격',
    phone         varchar(255)                           null comment '전화번호',
    is_phone_open tinyint(1)   default 0                 not null comment '전화번호 공개 여부(0:비공개 / 1:공개)',
    is_deleted    tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at    timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at    timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    thumbnail     varchar(510)                           null comment '썸네일 이미지 링크',
    hit           int unsigned default 0                 not null comment '조회수',
    ip            varchar(45)                            not null comment 'IP 주소'
)
    collate = utf8_unicode_ci;

create table if not exists land_comments
(
    id         int unsigned auto_increment comment 'land comments 고유 id'
        primary key,
    user_id    int unsigned                         not null comment 'user 고유 id',
    land_id    int unsigned                         not null comment 'land(원룸) 고유 id',
    content    text                                 not null comment '내용',
    score      int unsigned                         not null comment '평점',
    nickname   varchar(50)                          not null comment '댓글 user의 닉네임',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists lands
(
    id                        int unsigned auto_increment comment 'lands 고유 id'
        primary key,
    name                      varchar(255)                         not null comment '건물 이름',
    internal_name             varchar(50)                          not null comment '건물 이름 소문자 변환 및 띄어쓰기 제거',
    size                      varchar(255)                         null comment '방 크기',
    room_type                 varchar(255)                         null comment '원룸 종류',
    latitude                  varchar(255)                         null comment '위도',
    longitude                 varchar(255)                         null comment '경도',
    phone                     varchar(255)                         null comment '전화번호',
    image_urls                text                                 null comment '이미지 링크',
    address                   text                                 null comment '주소',
    description               text                                 null comment '세부 사항',
    floor                     int unsigned                         null comment '층 수',
    deposit                   varchar(255)                         null comment '보증금',
    monthly_fee               varchar(255)                         null comment '월세',
    charter_fee               varchar(255)                         null comment '전세',
    management_fee            varchar(255)                         null comment '관리비',
    opt_refrigerator          tinyint(1) default 0                 not null comment '냉장고 보유 여부',
    opt_closet                tinyint(1) default 0                 not null comment '옷장 보유 여부',
    opt_tv                    tinyint(1) default 0                 not null comment 'tv 보유 여부',
    opt_microwave             tinyint(1) default 0                 not null comment '전자레인지 보유 여부',
    opt_gas_range             tinyint(1) default 0                 not null comment '가스레인지 보유 여부',
    opt_induction             tinyint(1) default 0                 not null comment '인덕션 보유 여부',
    opt_water_purifier        tinyint(1) default 0                 not null comment '정수기 보유 여부',
    opt_air_conditioner       tinyint(1) default 0                 not null comment '에어컨 보유 여부',
    opt_washer                tinyint(1) default 0                 not null comment '샤워기 보유 여부',
    opt_bed                   tinyint(1) default 0                 not null comment '침대 보유 여부',
    opt_desk                  tinyint(1) default 0                 not null comment '책상 보유 여부',
    opt_shoe_closet           tinyint(1) default 0                 not null comment '신발장 보유 여부',
    opt_electronic_door_locks tinyint(1) default 0                 not null comment '전자 도어락 보유 여부',
    opt_bidet                 tinyint(1) default 0                 not null comment '비데 보유 여부',
    opt_veranda               tinyint(1) default 0                 not null comment '베란다 보유 여부',
    opt_elevator              tinyint(1) default 0                 not null comment '엘레베이터 보유 여부',
    is_deleted                tinyint(1) default 0                 not null comment '삭제 여부',
    created_at                timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at                timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint ux_name
        unique (name)
)
    collate = utf8_unicode_ci;

create index ix_internalname
    on lands (internal_name);

create table if not exists lectures
(
    id             int unsigned auto_increment comment 'lectures 고유 id'
        primary key,
    semester_date  varchar(6)                          not null comment '학기',
    code           varchar(10)                         not null comment '강의 코드',
    name           varchar(50)                         not null comment '강의 이름',
    grades         varchar(2)                          not null comment '대상 학년',
    class          varchar(3)                          not null comment '강의 분반',
    regular_number varchar(4)                          not null comment '수강 인원',
    department     varchar(30)                         not null comment '강의 학과',
    target         varchar(200)                        not null comment '강의 대상',
    professor      varchar(30)                         null comment '강의 교수',
    is_english     varchar(2)                          not null comment '영어강의 여부',
    design_score   varchar(2)                          not null comment '설계 학점',
    is_elearning   varchar(2)                          not null comment '이러닝 여부',
    class_time     varchar(100)                        not null comment '강의 시간',
    created_at     timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at     timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    charset = utf8;

create table if not exists lost_item_comments
(
    id           int unsigned auto_increment comment 'lost item comments 고유 id'
        primary key,
    lost_item_id int unsigned                         not null comment 'lost item (분실물) 고유 id',
    content      text                                 not null comment '내용',
    user_id      int unsigned                         not null comment '답글 user 고유 id',
    nickname     varchar(50)                          not null comment '답글 user의 닉네임',
    is_deleted   tinyint(1) default 0                 not null comment '삭제 여부',
    created_at   timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists lost_item_view_logs
(
    id           int unsigned auto_increment comment 'lost item view logs 고유 id'
        primary key,
    lost_item_id int unsigned not null comment 'lost_item(분실물) 고유 id',
    user_id      int unsigned null comment 'user 고유 id',
    expired_at   timestamp    null comment '만료 시간',
    ip           varchar(45)  not null comment 'IP 주소',
    constraint lost_item_view_logs_lost_item_id_user_id_unique
        unique (lost_item_id, user_id)
)
    collate = utf8_unicode_ci;

create table if not exists lost_items
(
    id            int unsigned auto_increment comment 'lost items 고유 id'
        primary key,
    type          int unsigned                               not null comment '서비스 타입(0:습득 서비스 / 1:분실 서비스)',
    title         varchar(255)                               not null comment '제목',
    location      varchar(255)                               null comment '분실물 위치',
    content       text                                       null comment '내용',
    user_id       int unsigned                               not null comment '작성자 user 고유 id',
    nickname      varchar(50)                                not null comment '작성자 user 닉네임',
    state         int unsigned     default 0                 not null comment '상태정보(0: 찾는중 / 1:돌려받음)',
    phone         varchar(255)                               null comment '전화 번호',
    is_phone_open tinyint(1)       default 0                 not null comment '전화번호 공개 여부(0:비공개 / 1:공개)',
    image_urls    text                                       null comment '이미지 링크',
    is_deleted    tinyint(1)       default 0                 not null comment '삭제 여부',
    created_at    timestamp        default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at    timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    thumbnail     varchar(510)                               null comment '썸네일 이미지',
    hit           int unsigned     default 0                 not null comment '조회수',
    ip            varchar(45)                                not null comment 'IP 주소',
    comment_count tinyint unsigned default 0                 not null comment '전체 댓글 수',
    date          date                                       null comment '분실 물품 날짜'
)
    collate = utf8_unicode_ci;

create table if not exists members
(
    id             int auto_increment comment 'members 고유 id'
        primary key,
    name           varchar(50)                          not null comment '이름',
    student_number varchar(255)                         null comment '학번',
    track_id       int unsigned                         not null comment '소속 트랙 고유 id',
    position       varchar(255)                         not null comment '직급',
    email          varchar(100)                         null comment '이메일',
    image_url      text                                 null comment '이미지 링크',
    is_deleted     tinyint(1) default 0                 not null comment '삭제 여부',
    created_at     timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at     timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists migrations
(
    id        int unsigned auto_increment
        primary key,
    migration varchar(255) not null,
    batch     int          not null
)
    collate = utf8_unicode_ci;

create table if not exists notice_articles
(
    id            int unsigned auto_increment comment 'notice articles 고유 id'
        primary key,
    board_id      int unsigned                           not null comment '게시판 고유 id',
    title         varchar(255)                           not null comment '제목',
    content       mediumtext                             null comment '내용',
    author        varchar(50)                            not null comment '작성자',
    hit           int unsigned default 0                 not null comment '조회수',
    is_deleted    tinyint(1)   default 0                 not null comment '삭제 여부',
    article_num   int unsigned                           not null comment '게시물 번호',
    permalink     varchar(100)                           not null comment '기존 게시글 url',
    has_notice    tinyint(1)   default 0                 not null comment '기존에 올라왔는지 여부',
    registered_at varchar(255)                           null comment '등록 일자',
    created_at    timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at    timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint ux_notice_article
        unique (board_id, article_num)
)
    collate = utf8_unicode_ci;

create table if not exists owners
(
    user_id                                    int                      not null comment 'user 고유 id'
        primary key,
    company_registration_number                varchar(12) charset utf8 null comment '사업자등록번호',
    company_registration_certificate_image_url varchar(255)             null,
    grant_shop                                 tinyint default 0        null comment '상점 수정 권한',
    grant_event                                tinyint default 0        null comment '이벤트 수정 권한',
    constraint company_registration_number_UNIQUE
        unique (company_registration_number)
)
    collate = utf8_bin;

create table if not exists password_resets
(
    email      varchar(255) not null,
    token      varchar(255) not null,
    created_at timestamp    null
)
    collate = utf8_unicode_ci;

create index password_resets_email_index
    on password_resets (email);

create table if not exists search_articles
(
    id         int unsigned auto_increment comment 'search articles 고유 id'
        primary key,
    table_id   int unsigned                         not null comment '게시판(table) 고유 id',
    article_id int unsigned                         not null comment 'article(게시글) 고유 id',
    title      varchar(255)                         not null comment '게시글 제목',
    content    text                                 null comment '게시글 내용',
    user_id    int unsigned                         null comment 'user 고유 id',
    nickname   varchar(50)                          not null comment '닉네임',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint idx_unique
        unique (table_id, article_id),
    constraint pk
        unique (id)
)
    collate = utf8_bin;

create index idx_is_deleted
    on search_articles (is_deleted);

create index idx_nickname
    on search_articles (nickname, is_deleted, created_at);

create index idx_timestamp
    on search_articles (created_at);

create table if not exists semester
(
    id       int unsigned auto_increment
        primary key,
    semester varchar(10) not null comment '학기',
    constraint semester_UNIQUE
        unique (semester)
)
    collate = utf8_bin;

create table if not exists shop_categories
(
    id         int unsigned auto_increment comment 'shop_categories 고유 id'
        primary key,
    name       varchar(255)                         not null comment '카테고리 이름',
    image_url  varchar(255)                         null comment '이미지 URL',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists shop_category_map
(
    id               int unsigned auto_increment comment 'shop_category_map 고유 id'
        primary key,
    shop_id          int unsigned                        not null comment 'shops 고유 id',
    shop_category_id int unsigned                        not null comment 'shop_categories 고유 id',
    created_at       timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint SHOP_ID_AND_SHOP_CATEGORY_ID
        unique (shop_id, shop_category_id)
)
    collate = utf8_bin;

create table if not exists shop_images
(
    id         int unsigned auto_increment comment 'shop_images 고유 id'
        primary key,
    shop_id    int unsigned                        not null comment 'shops 고유 id',
    image_url  varchar(255)                        null comment '이미지 URL',
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint SHOP_ID_AND_IMAGE_URL
        unique (shop_id, image_url)
)
    collate = utf8_bin;

create table if not exists shop_menu_categories
(
    id         int unsigned auto_increment comment 'shop_menu_categories 고유 id'
        primary key,
    shop_id    int unsigned                         not null comment 'shops 고유 id',
    name       varchar(255)                         not null comment '카테고리 이름',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists shop_menu_category_map
(
    id                    int unsigned auto_increment comment 'shop_menu_category_map 고유 id'
        primary key,
    shop_menu_id          int unsigned                        not null comment 'shop_menus 고유 id',
    shop_menu_category_id int unsigned                        not null comment 'shop_menu_categories 고유 id',
    created_at            timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at            timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint SHOP_MENU_ID_AND_SHOP_MENU_CATEGORY_ID
        unique (shop_menu_id, shop_menu_category_id)
)
    collate = utf8_bin;

create table if not exists shop_menu_details
(
    id           int unsigned auto_increment comment 'shop_menu_details 고유 id'
        primary key,
    shop_menu_id int unsigned                        not null comment 'shop_menus 고유 id',
    `option`     varchar(255)                        null comment '옵션 이름',
    price        int unsigned                        not null comment '가격',
    created_at   timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint SHOP_MENU_ID_AND_OPTION_AND_PRICE
        unique (shop_menu_id, `option`, price)
)
    collate = utf8_bin;

create table if not exists shop_menu_images
(
    id           int unsigned auto_increment comment 'shop_menu_images 고유 id'
        primary key,
    shop_menu_id int unsigned                        not null comment 'shop_menus 고유 id',
    image_url    varchar(255)                        not null comment '이미지 URL',
    created_at   timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint SHOP_MENU_ID_AND_IMAGE_URL
        unique (shop_menu_id, image_url)
)
    collate = utf8_bin;

create table if not exists shop_menus
(
    id          int unsigned auto_increment comment 'shop_menus 고유 id'
        primary key,
    shop_id     int unsigned                         not null comment 'shop 고유 id',
    name        varchar(255)                         not null comment '메뉴 이름',
    description varchar(255)                         null comment '메뉴 구성',
    is_hidden   tinyint(1) default 0                 not null comment '숨김 여부',
    is_deleted  tinyint(1) default 0                 not null comment '삭제 여부',
    created_at  timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists shop_opens
(
    id          int unsigned auto_increment comment 'shop_open 고유 id'
        primary key,
    shop_id     int unsigned                         not null comment 'shops 고유 id',
    day_of_week varchar(10)                          not null comment '요일',
    closed      tinyint(1)                           not null comment '휴무 여부',
    open_time   varchar(10)                          null comment '오픈 시간',
    close_time  varchar(10)                          null comment '마감 시간',
    is_deleted  tinyint(1) default 0                 not null comment '삭제 여부',
    created_at  timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists shop_view_logs
(
    id         int unsigned auto_increment comment 'shop_view_logs 고유 id'
        primary key,
    shop_id    int unsigned not null comment '가게 고유 id',
    user_id    int unsigned null comment 'user 고유 id',
    expired_at timestamp    null comment '만료 시간',
    ip         varchar(45)  not null comment '조회한 IP 주소'
)
    collate = utf8_bin;

create table if not exists shops
(
    id             int unsigned auto_increment comment 'shops 고유 id'
        primary key,
    owner_id       int                                    null comment 'owner 고유 id',
    name           varchar(50)                            not null comment '가게 이름',
    internal_name  varchar(50)                            not null comment '가게 이름을 소문자로 변경하고 띄어쓰기 제거',
    chosung        varchar(3)                             null comment '가게 이름 앞자리 1글자의 초성',
    phone          varchar(50)                            null comment '전화 번호',
    address        text                                   null comment '주소',
    description    text                                   null comment '세부 사항',
    delivery       tinyint(1)   default 0                 not null comment '배달 가능 여부',
    delivery_price int unsigned default 0                 not null comment '배달 금액',
    pay_card       tinyint(1)   default 0                 not null comment '카드 가능 여부',
    pay_bank       tinyint(1)   default 0                 not null comment '계좌이체 가능 여부',
    is_deleted     tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at     timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at     timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    is_event       tinyint(1)   default 0                 not null comment '이벤트 진행 여부',
    remarks        text                                   null comment '이벤트 상세내용 등 부가내용',
    hit            int unsigned default 0                 not null comment '조회수'
)
    charset = utf8mb4;

create index ix_internalname
    on shops (internal_name);

create table if not exists students
(
    user_id            int          not null comment 'user 고유 id'
        primary key,
    anonymous_nickname varchar(255) null comment '익명 닉네임',
    student_number     varchar(255) null comment '학번',
    major              varchar(50)  null comment '전공',
    identity           smallint     null comment '신원(0: 학생, 1: 대학원생)',
    is_graduated       tinyint(1)   null comment '졸업 여부',
    constraint anonymous_nickname_UNIQUE
        unique (anonymous_nickname)
)
    collate = utf8_bin;

create table if not exists survey_answers
(
    id          int unsigned auto_increment
        primary key,
    survey_id   int unsigned                         not null,
    question_id int unsigned                         not null,
    content     text                                 not null,
    is_deleted  tinyint(1) default 0                 not null,
    created_at  timestamp  default CURRENT_TIMESTAMP not null,
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    collate = utf8_unicode_ci;

create table if not exists survey_questions
(
    id          int unsigned auto_increment
        primary key,
    survey_id   int unsigned                           not null,
    `order`     int unsigned                           not null,
    order_sub   int unsigned default 0                 not null,
    type        varchar(255)                           not null,
    title       varchar(255)                           not null,
    choices     text                                   not null,
    is_deleted  tinyint(1)   default 0                 not null,
    is_required tinyint(1)   default 0                 not null,
    created_at  timestamp    default CURRENT_TIMESTAMP not null,
    updated_at  timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    collate = utf8_unicode_ci;

create table if not exists survey_view_logs
(
    id         int unsigned auto_increment
        primary key,
    survey_id  int unsigned not null,
    expired_at timestamp    null,
    ip         varchar(45)  not null,
    constraint survey_view_logs_survey_id_ip_unique
        unique (survey_id, ip)
)
    collate = utf8_unicode_ci;

create table if not exists surveys
(
    id             int unsigned auto_increment
        primary key,
    user_id        int unsigned                           not null,
    title          varchar(255)                           not null,
    description    varchar(255)                           null,
    conclusion     varchar(255)                           null,
    is_answer_open tinyint(1)   default 0                 not null,
    started_at     timestamp                              null,
    finished_at    timestamp                              null,
    state          tinyint      default 0                 not null,
    is_deleted     tinyint(1)   default 0                 not null,
    created_at     timestamp    default CURRENT_TIMESTAMP not null,
    updated_at     timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    hit            int unsigned default 0                 not null,
    tag            varchar(255)                           null,
    is_recruit     tinyint(1)   default 0                 not null
)
    collate = utf8_unicode_ci;

create table if not exists tech_stacks
(
    id          int unsigned auto_increment comment 'tech_stacks 고유 id'
        primary key,
    image_url   text                                 null comment '이미지 링크',
    name        varchar(50)                          not null comment '기술 스택 명',
    description varchar(100)                         null comment '기술 스택 설명',
    track_id    int unsigned                         not null comment 'track 고유 id',
    is_deleted  tinyint(1) default 0                 not null comment '삭제 여부',
    created_at  timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists temp_articles
(
    id            int unsigned auto_increment comment 'temp_articles 고유 id'
        primary key,
    title         varchar(255)                               not null comment '제목',
    content       text                                       null comment '내용',
    nickname      varchar(50)                                not null comment '작성자 닉네임',
    password      text                                       not null comment '익명게시글 비밀번호',
    is_deleted    tinyint(1)       default 0                 not null comment '삭제 여부',
    created_at    timestamp        default CURRENT_TIMESTAMP not null comment '작성 일자',
    updated_at    timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    hit           int unsigned     default 0                 not null comment '조회수',
    comment_count tinyint unsigned default 0                 not null comment '전체 댓글 수'
)
    collate = utf8_unicode_ci;

create table if not exists temp_comments
(
    id         int unsigned auto_increment comment 'temp_comments 고유 id'
        primary key,
    article_id int unsigned                         not null comment 'article(게시글) 고유 id',
    content    text                                 not null comment '내용',
    nickname   varchar(50)                          not null comment '작성자 닉네임',
    password   text                                 not null comment '익명댓글 비밀번호',
    is_deleted tinyint(1) default 0                 not null comment '삭제 여부',
    created_at timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_unicode_ci;

create table if not exists test
(
    id             int unsigned auto_increment
        primary key,
    portal_account varchar(50) not null,
    constraint users_portal_account_unique
        unique (portal_account)
)
    collate = utf8_unicode_ci;

create table if not exists timetables
(
    id             int unsigned auto_increment comment 'timetables 고유 id'
        primary key,
    user_id        int unsigned                         not null comment 'user 고유 id',
    semester_id    int unsigned                         not null comment '학기 고유 id',
    code           varchar(10)                          null comment '과목 코드',
    class_title    varchar(50)                          not null comment '과목 명',
    class_time     varchar(100)                         not null comment '과목 시간',
    class_place    varchar(30)                          null comment '수업 장소',
    professor      varchar(30)                          null comment '담당 교수',
    grades         varchar(2)                           not null comment '학점',
    lecture_class  varchar(3)                           null comment '분반',
    target         varchar(200)                         null comment '학년 대상',
    regular_number varchar(4)                           null comment '수강 정원',
    design_score   varchar(4)                           null comment '설계',
    department     varchar(30)                          null comment '학부',
    memo           varchar(200)                         null comment '사용자용 메모',
    is_deleted     tinyint(1) default 0                 not null comment '삭제 여부',
    created_at     timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at     timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists tracks
(
    id         int unsigned auto_increment comment 'tracks 테이블 고유 id'
        primary key,
    name       varchar(50)                            not null comment '트랙명',
    headcount  int unsigned default 0                 not null comment '인원수',
    is_deleted tinyint(1)   default 0                 not null comment '삭제 여부',
    created_at timestamp    default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자'
)
    collate = utf8_bin;

create table if not exists users
(
    id                int unsigned auto_increment comment 'users 테이블 고유 id'
        primary key,
    password          text                                 not null comment '비밀번호',
    nickname          varchar(50)                          null comment '닉네임',
    name              varchar(50)                          null comment '이름',
    phone_number      varchar(255)                         null comment '휴대 전화 번호',
    user_type         varchar(255)                         not null comment '유저 타입(Students or Owners)',
    email             varchar(100)                         not null comment '학교 email',
    gender            int unsigned                         null comment '성별',
    is_authed         tinyint(1) default 0                 not null comment '인증 여부',
    last_logged_at    timestamp                            null comment '최근 로그인 일자',
    profile_image_url varchar(255)                         null comment '프로필 이미지 s3 url',
    is_deleted        tinyint(1) default 0                 not null comment '탈퇴 여부',
    created_at        timestamp  default CURRENT_TIMESTAMP not null comment '회원가입 일자(생성 일자)',
    updated_at        timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    auth_token        varchar(255)                         null comment '이메일 인증 토큰',
    auth_expired_at   varchar(255)                         null comment '이메일 인증 토큰 만료 시간',
    reset_token       varchar(255)                         null comment '비밀번호 초기화 토큰',
    reset_expired_at  varchar(255)                         null comment '비밀번호 초기화 토큰 만료 시간',
    constraint email_UNIQUE
        unique (email),
    constraint nickname_UNIQUE
        unique (nickname)
)
    collate = utf8_unicode_ci;

create table if not exists owner_attachments
(
    id         int unsigned auto_increment
        primary key,
    owner_id   int unsigned                         not null,
    url        text                                 not null,
    is_deleted tinyint(1) default 0                 not null,
    created_at timestamp  default CURRENT_TIMESTAMP not null,
    updated_at timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint owner_shop_attachment_fk_owner_id
        foreign key (owner_id) references users (id)
)
    collate = utf8_bin;

create table if not exists users_owners
(
    id      int unsigned auto_increment comment 'users_owners 고유 id'
        primary key,
    user_id int unsigned not null comment 'users 중에 owners에 해당하는 user_id',
    email   varchar(125) null comment '연락 가능한 이메일',
    constraint email_UNIQUE
        unique (email),
    constraint user_id_UNIQUE
        unique (user_id),
    constraint users_owners_fk_user_id
        foreign key (user_id) references users (id)
            on delete cascade
)
    collate = utf8_bin;

create table if not exists versions
(
    id         int unsigned auto_increment comment 'versions 테이블 고유 id'
        primary key,
    version    varchar(255)                        not null comment '버전 명 (예시 : 1.1.0)',
    type       varchar(255)                        null,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '생성일자',
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    constraint versions_type_unique
        unique (type)
)
    collate = utf8_unicode_ci;
