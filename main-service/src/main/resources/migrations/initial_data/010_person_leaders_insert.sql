--liquibase formatted sql

--changeset alex_pletnev:10
insert into person (email, username, password, person_race_id, a_leader)
values ('ntn2_03@mail.ru', 'Aragorn', 'Aragorn123', 1, true),
       ('elrond@tolkin.com', 'Elrond', 'Elrond123', 2, true),
       ('thorin_oakenshield@tolkin.com', 'Thorin Oakenshield', 'ThorinOakenshield123', 3, true),
       ('bilbo_baggins@tolkin.com', 'Bilbo Baggins', 'BilboBaggins123', 4, true),
       ('sauron@tolkin.com', 'Sauron', 'Sauron123', 5, true),
       ('i.ponomarev.1991@gmail.com', 'The Great Goblin', 'TheGreatGoblin123', 6, true)
;
--rollback DELETE FROM person WHERE email IN
--rollback ('ntn2_03@mail.ru',
--rollback 'elrond@tolkin.com',
--rollback 'thorin_oakenshield@tolkin.com',
--rollback 'bilbo_baggins@tolkin.com',
--rollback 'sauron@tolkin.com',
--rollback 'i.ponomarev.1991@gmail.com');


