--liquibase formatted sql

--changeset ivan_ponomarev:5
create table recipe_ingredient_relation
(
    id            bigserial unique not null,
    recipe_id     bigint           not null,
    ingredient_id bigint           not null,
    primary key (id),
    unique (recipe_id, ingredient_id)
)
--rollback drop table recipe_ingredient_relation