--liquibase formatted sql

--changeset stats_user:1
create table person
(
    statistic_person_id serial primary key,
    email               text unique not null,
    person_race_id      bigint      not null,
    rating_count        bigint default 0
)
--rollback drop table person
