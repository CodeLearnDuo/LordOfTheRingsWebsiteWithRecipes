--liquibase formatted sql

--changeset ivan_ponomarev:7
create table statistic
(
    id        bigserial unique not null,
    person_id bigint           not null,
    recipe_id bigint           not null,
    value     boolean          not null,
    at        timestamp        not null,
    primary key (id),
    unique (person_id, recipe_id)
)
--rollback drop table statistic