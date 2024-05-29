--liquibase formatted sql

--changeset ivan_ponomarev:6
create table race_relationship
(
    id             bigserial unique not null,
    person_race_id bigint           not null,
    recipe_race_id bigint           not null,
    relationship_coefficient numeric not null ,
    primary key (id),
    unique (person_race_id, recipe_race_id)
)
--rollback drop table race_relationship