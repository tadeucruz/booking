CREATE TABLE IF NOT EXISTS bookings
  (
     id         VARCHAR(36) PRIMARY KEY,
     room_id    VARCHAR(36),
     user_id    VARCHAR(36),
     start_date TIMESTAMP,
     end_date   TIMESTAMP
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