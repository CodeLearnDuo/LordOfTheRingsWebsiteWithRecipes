--liquibase formatted sql

--changeset ivan_ponomarev:6
create table race_relationship
(
    id   bigint unique not null,
    person_race_id bigint unique not null,
    recipe_race_id bigint unique not null,
    relationship_coefficient numeric not null ,
    primary key (id)
)
--rollback drop table race_relationship