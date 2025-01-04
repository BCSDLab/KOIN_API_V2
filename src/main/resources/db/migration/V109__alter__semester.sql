alter table semester
    add column year int unsigned,
    add column term varchar(20);

update semester set year = 2019, term = 'SECOND' where semester = '20192';
update semester set year = 2019, term = 'SUMMER' where semester = '2019-여름';
update semester set year = 2019, term = 'WINTER' where semester = '2019-겨울';
update semester set year = 2020, term = 'FIRST' where semester = '20201';
update semester set year = 2020, term = 'SECOND' where semester = '20202';
update semester set year = 2020, term = 'SUMMER' where semester = '2020-여름';
update semester set year = 2020, term = 'WINTER' where semester = '2020-겨울';
update semester set year = 2021, term = 'FIRST' where semester = '20211';
update semester set year = 2021, term = 'SECOND' where semester = '20212';
update semester set year = 2021, term = 'SUMMER' where semester = '2021-여름';
update semester set year = 2021, term = 'WINTER' where semester = '2021-겨울';
update semester set year = 2022, term = 'FIRST' where semester = '20221';
update semester set year = 2022, term = 'SECOND' where semester = '20222';
update semester set year = 2022, term = 'SUMMER' where semester = '2022-여름';
update semester set year = 2022, term = 'WINTER' where semester = '2022-겨울';
update semester set year = 2023, term = 'FIRST' where semester = '20231';
update semester set year = 2023, term = 'SECOND' where semester = '20232';
update semester set year = 2023, term = 'SUMMER' where semester = '2023-여름';
update semester set year = 2023, term = 'WINTER' where semester = '2023-겨울';
update semester set year = 2024, term = 'FIRST' where semester = '20241';
update semester set year = 2024, term = 'SECOND' where semester = '20242';
update semester set year = 2024, term = 'SUMMER' where semester = '2024-여름';
update semester set year = 2024, term = 'WINTER' where semester = '2024-겨울';

ALTER TABLE semester
    MODIFY COLUMN year INT UNSIGNED NOT NULL,
    MODIFY COLUMN term VARCHAR(20) NOT NULL;