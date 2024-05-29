--liquibase formatted sql

--changeset alex_pletnev:8
insert into race (name)
values ('people'),
       ('elves'),
       ('dwarfs'),
       ('hobbits'),
       ('orcs'),
       ('goblins')
;
--rollback DELETE FROM race WHERE name IN ('people', 'elfs', 'dwarfs', 'hobbits', 'orcs', 'goblins');
