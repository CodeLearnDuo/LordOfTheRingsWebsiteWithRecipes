--liquibase formatted sql

--changeset alex_pletnev:1
create table race
(
    id bigserial unique not null,
    name text unique   not null,
    primary key (id)
)
--rollback drop table race