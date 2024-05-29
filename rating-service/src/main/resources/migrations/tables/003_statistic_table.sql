--liquibase formatted sql

--changeset stats_user:3
create table statistic
(
    id           bigserial unique not null ,
    person_email text      not null references person (email),
    recipe_id    bigint    not null references recipe (id),
    value        numeric   not null,
    at           timestamp not null,
    primary key (id)
)
--rollback drop table statistic
