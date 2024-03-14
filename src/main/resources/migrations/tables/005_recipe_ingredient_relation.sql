--liquibase formatted sql

--changeset ivan_ponomarev:5
create table recipe_ingredient_relation
(
    id   bigint unique not null,
    recipe_id   bigint unique not null,
    ingredient_id   bigint unique not null,
    primary key (id)
)
--rollback drop table recipe_ingredient_relation