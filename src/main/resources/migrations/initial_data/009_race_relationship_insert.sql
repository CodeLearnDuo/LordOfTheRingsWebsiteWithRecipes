--liquibase formatted sql

--changeset alex_pletnev:9
insert into race_relationship (person_race_id, recipe_race_id, relationship_coefficient)
values (1, 1, 0.2),
       (1, 2, 0.5),
       (1, 3, 0.5),
       (1, 4, 0.5),
       (1, 5, 0.9),
       (1, 6, 0.9),
       (2, 1, 0.5),
       (2, 2, 0.2),
       (2, 3, 0.8),
       (2, 4, 0.5),
       (2, 5, 0.9),
       (2, 6, 0.9),
       (3, 1, 0.5),
       (3, 2, 0.8),
       (3, 3, 0.2),
       (3, 4, 0.3),
       (3, 5, 0.9),
       (3, 6, 0.9),
       (4, 1, 0.5),
       (4, 2, 0.5),
       (4, 3, 0.5),
       (4, 4, 0.2),
       (4, 5, 0.9),
       (4, 6, 0.9),
       (5, 1, 0.9),
       (5, 2, 0.9),
       (5, 3, 0.9),
       (5, 4, 0.9),
       (5, 5, 0.2),
       (5, 6, 0.4),
       (6, 1, 0.9),
       (6, 2, 0.9),
       (6, 3, 0.9),
       (6, 4, 0.9),
       (6, 5, 0.4),
       (6, 6, 0.2)
;
--rollback DELETE FROM race_relationship WHERE
--rollback (person_race_id = 1 AND recipe_race_id = 1 AND relationship_coefficient = 0.2) OR
--rollback (person_race_id = 1 AND recipe_race_id = 2 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 1 AND recipe_race_id = 3 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 1 AND recipe_race_id = 4 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 1 AND recipe_race_id = 5 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 1 AND recipe_race_id = 6 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 2 AND recipe_race_id = 1 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 2 AND recipe_race_id = 2 AND relationship_coefficient = 0.2) OR
--rollback (person_race_id = 2 AND recipe_race_id = 3 AND relationship_coefficient = 0.8) OR
--rollback (person_race_id = 2 AND recipe_race_id = 4 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 2 AND recipe_race_id = 5 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 2 AND recipe_race_id = 6 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 3 AND recipe_race_id = 1 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 3 AND recipe_race_id = 2 AND relationship_coefficient = 0.8) OR
--rollback (person_race_id = 3 AND recipe_race_id = 3 AND relationship_coefficient = 0.2) OR
--rollback (person_race_id = 3 AND recipe_race_id = 4 AND relationship_coefficient = 0.3) OR
--rollback (person_race_id = 3 AND recipe_race_id = 5 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 3 AND recipe_race_id = 6 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 4 AND recipe_race_id = 1 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 4 AND recipe_race_id = 2 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 4 AND recipe_race_id = 3 AND relationship_coefficient = 0.5) OR
--rollback (person_race_id = 4 AND recipe_race_id = 4 AND relationship_coefficient = 0.2) OR
--rollback (person_race_id = 4 AND recipe_race_id = 5 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 4 AND recipe_race_id = 6 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 5 AND recipe_race_id = 1 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 5 AND recipe_race_id = 2 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 5 AND recipe_race_id = 3 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 5 AND recipe_race_id = 4 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 5 AND recipe_race_id = 5 AND relationship_coefficient = 0.2) OR
--rollback (person_race_id = 5 AND recipe_race_id = 6 AND relationship_coefficient = 0.4) OR
--rollback (person_race_id = 6 AND recipe_race_id = 1 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 6 AND recipe_race_id = 2 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 6 AND recipe_race_id = 3 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 6 AND recipe_race_id = 4 AND relationship_coefficient = 0.9) OR
--rollback (person_race_id = 6 AND recipe_race_id = 5 AND relationship_coefficient = 0.4) OR
--rollback (person_race_id = 6 AND recipe_race_id = 6 AND relationship_coefficient = 0.2) OR