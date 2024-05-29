--liquibase formatted sql

--changeset stats_user:2
create table recipe
(
    id      bigserial unique not null,
    title   text   not null,
    race_id bigint not null,
    rating  numeric default 0,
    primary key (id)
)
--rollback drop table recipe
