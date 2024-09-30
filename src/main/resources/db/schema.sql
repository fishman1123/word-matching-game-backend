CREATE SCHEMA IF NOT EXISTS word_bridge_DB;

USE word_bridge_DB;


CREATE TABLE IF NOT EXISTS login
(
    id           int AUTO_INCREMENT
        PRIMARY KEY,
    social_email varchar(200) NULL,
    username     varchar(200) NULL
);

CREATE TABLE IF NOT EXISTS room_info
(
    id                       int    NULL,
    in_game                  int(1) NULL,
    room_location_longtitude double NULL,
    room_location_latitude   double NULL,
    CONSTRAINT room_info_pk
        UNIQUE (id),
    CONSTRAINT room_info_login_id_fk
        FOREIGN KEY (id) REFERENCES word_bridge_db.login (id)
);

CREATE TABLE IF NOT EXISTS room_status_info
(
    id                int    NULL,
    entered_player_id int    NULL,
    host_is_ready     int(1) NULL,
    visitor_is_ready  int(1) NULL,
    CONSTRAINT room_status_info_login_id_fk
        FOREIGN KEY (id) REFERENCES word_bridge_db.login (id),
    CONSTRAINT room_status_info_login_id_fk_2
        FOREIGN KEY (entered_player_id) REFERENCES word_bridge_db.login (id)
);

CREATE TABLE IF NOT EXISTS score
(
    id         int NULL,
    user_score int NULL,
    CONSTRAINT score_login_id_fk
        FOREIGN KEY (id) REFERENCES word_bridge_db.login (id)
);

CREATE TABLE IF NOT EXISTS user_information
(
    id              int    NULL,
    user_win        int    NULL,
    user_lose       int    NULL,
    user_has_room   int(2) NULL,
    is_user_playing int(2) NULL,
    CONSTRAINT user_information_login_id_fk
        FOREIGN KEY (id) REFERENCES word_bridge_db.login (id)
);
