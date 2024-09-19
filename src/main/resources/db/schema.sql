CREATE SCHEMA IF NOT EXISTS word_bridge_DB;

USE word_bridge_DB;

CREATE TABLE IF NOT EXISTS chat
(
    chat_id          int          not null
    primary key,
    user_sender_id   int          null,
    user_receiver_id int          null,
    chat_log_url     varchar(200) null,
    constraint chat_login_id_fk
    foreign key (user_sender_id) references word_bridge_db.login (id),
    constraint chat_login_id_fk_2
    foreign key (user_receiver_id) references word_bridge_db.login (id)
    );

CREATE TABLE IF NOT EXISTS login
(
    id           int auto_increment
    primary key,
    social_email varchar(200) null,
    username     varchar(200) null
    );

CREATE TABLE IF NOT EXISTS room_info
(
    id                       int    null,
    in_game                  int(2) null,
    room_location_longtitude double null,
    room_location_latitude   double null,
    constraint room_info_login_id_fk
    foreign key (id) references word_bridge_db.login (id)
    );

CREATE TABLE IF NOT EXISTS score
(
    id         int null,
    user_score int null,
    constraint score_login_id_fk
    foreign key (id) references word_bridge_db.login (id)
    );

CREATE TABLE IF NOT EXISTS user_information
(
    id              int    null,
    user_win        int    null,
    user_lose       int    null,
    user_has_room   int(2) null,
    is_user_playing int(2) null,
    constraint user_information_login_id_fk
    foreign key (id) references word_bridge_db.login (id)
    );
