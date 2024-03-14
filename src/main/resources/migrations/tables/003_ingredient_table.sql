--liquibase formatted sql

--changeset ivan_ponomarev:3
create table ingredient
(
    id   bigint unique not null,
    name text unique   not null,
    description text not null,
    primary key (id)
)
--rollback drop table ingredient