--liquibase formatted sql

--changeset ivan_ponomarev:4
create table recipe
(
    id          bigint unique not null,
    title       text          not null,
    description text          not null,
    logo        bytea,
    race_id     bigint unique not null,
    rank        numeric default 0,
    primary key (id)
)
--rollback drop table recipe