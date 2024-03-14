--liquibase formatted sql

--changeset ivan_ponomarev:7
create table statistic
(
    id   bigint unique not null,
    person_id   bigint unique not null,
    recipe_id   bigint unique not null,
    primary key (id)
)
--rollback drop table statistic