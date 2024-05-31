--liquibase formatted sql

--changeset stats_user:4

create table leader
(
    statistic_leader_id serial primary key,
    email               text          not null,
    race_id             bigint unique not null,
    raceTitle           text          not null,
)
--rollback drop table statistic
