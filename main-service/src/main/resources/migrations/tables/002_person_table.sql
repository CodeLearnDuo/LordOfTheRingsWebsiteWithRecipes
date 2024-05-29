--liquibase formatted sql

--changeset alex_pletnev:2
create table person
(
    id bigserial unique not null,
    email          text unique   not null,
    username       text          not null,
    password       text          not null,
    person_race_id bigint        not null references race (id),
    a_leader       boolean,
    primary key (id)
)
--rollback drop table person