--liquibase formatted sql

--changeset stats_user:2
create table recipe
(
    statistic_recipe_id serial primary key,
    recipe_id           bigint unique not null,
    title               text   not null,
    race_id             bigint not null,
    rating              numeric
)
--rollback drop table recipe
