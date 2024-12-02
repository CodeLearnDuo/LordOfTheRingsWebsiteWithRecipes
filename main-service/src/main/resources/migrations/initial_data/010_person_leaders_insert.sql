--liquibase formatted sql

--changeset alex_pletnev:11
insert into person (email, username, password, person_race_id, a_leader)
values ('ntn2_03@mail.ru', 'Aragorn', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 1, true),
       ('elrond@tolkin.com', 'Elrond', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 2, true),
       ('thorin_oakenshield@tolkin.com', 'Thorin Oakenshield', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 3, true),
       ('bilbo_baggins@tolkin.com', 'Bilbo Baggins', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 4, true),
       ('sauron@tolkin.com', 'Sauron', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 5, true),
       ('i.ponomarev.1991@gmail.com', 'The Great Goblin', '$2a$12$U7yLmJios7bo2S9Rbh7vjOFffATpUbn0zmlieUtzYnlAb4GnglfQK', 6, true)
;

--rollback DELETE FROM person WHERE email IN
--rollback ('ntn2_03@mail.ru',
--rollback 'elrond@tolkin.com',
--rollback 'thorin_oakenshield@tolkin.com',
--rollback 'bilbo_baggins@tolkin.com',
--rollback 'sauron@tolkin.com',
--rollback 'i.ponomarev.1991@gmail.com');


