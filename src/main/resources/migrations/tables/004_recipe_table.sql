--liquibase formatted sql

--changeset ivan_ponomarev:4
create table recipe
(
    id          bigserial unique not null,
    title       text             not null,
    description text             not null,
    logo_url    text,
    race_id     bigint           not null,
    rank        numeric default 0,
    primary key (id)
)
--rollback drop table recipe