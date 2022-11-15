CREATE TABLE IF NOT EXISTS bookings
  (
     id         INT PRIMARY KEY AUTO_INCREMENT,
     room_id    INT,
     user_id    INT,
     status     VARCHAR(36),
     start_date DATETIME,
     end_date   DATETIME
  )
engine=innodb;

CREATE TABLE IF NOT EXISTS services_locks
  (
     id      VARCHAR(36) PRIMARY KEY,
     name    VARCHAR(100),
     version INT
  )
engine=innodb;

INSERT INTO services_locks
            (id,
             name,
             version)
VALUES      (1,
             "reservations",
             0);
