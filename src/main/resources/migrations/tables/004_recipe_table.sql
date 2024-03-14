--liquibase formatted sql

--changeset ivan_ponomarev:4
create table recipe
(
    id   bigint unique not null,
    title text unique   not null,
    description  text  not null,
    logo bytea,
    race_id   bigint unique not null,
    rank numeric,
    primary key (id)
)
--rollback drop table recipe