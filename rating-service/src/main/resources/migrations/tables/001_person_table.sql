--liquibase formatted sql

--changeset stats_user:1
create table person
(
    email          text unique not null,
    person_race_id bigint      not null,
    rating_count   bigint default 0,
    primary key (email)
)
--rollback drop table person
