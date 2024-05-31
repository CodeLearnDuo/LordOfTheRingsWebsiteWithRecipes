--liquibase formatted sql

--changeset stats_user:4

create table leader
(
    email     text          not null,
    race_id   bigint unique not null,
    raceTitle text          not null,
    primary key (race_id)
)
--rollback drop table statistic
