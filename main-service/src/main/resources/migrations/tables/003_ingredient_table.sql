--liquibase formatted sql

--changeset ivan_ponomarev:3
create table ingredient
(
    id bigserial unique not null,
    name text   not null,
    description text not null,
    primary key (id),
    unique (name, description)
)
--rollback drop table ingredient