# unzip csv files
cd /tmp/psqldata && unzip csv.zip && cd /

# create databases and roles
psql -U postgres -c 'create database snzts;' > /dev/null 2>&1
psql -U postgres -d snzts -c 'CREATE EXTENSION citext;'
psql -U postgres -d snzts -c 'CREATE EXTENSION postgis;' > /dev/null 2>&1
psql -U postgres -d snzts -c 'CREATE EXTENSION postgis_topology;' > /dev/null 2>&1

psql -U postgres -c 'create user webuser;' > /dev/null 2>&1
psql -U postgres -c "alter user webuser with encrypted password 'webuser';" > /dev/null 2>&1
psql -U postgres -c 'grant all privileges on database snzts to webuser;' > /dev/null 2>&1


# load csv data
printf "subject...\n"
psql -U postgres -d snzts -c "CREATE TABLE subject (subject_code citext, title_text citext);" > /dev/null 2>&1
psql -U postgres -d snzts -c "COPY subject (subject_code, title_text) FROM '/tmp/psqldata/subject.csv' DELIMITER ',' CSV HEADER;" > /dev/null 2>&1

printf "family...\n"
psql -U postgres -d snzts -c "CREATE TABLE family (subject_code citext, family_code citext, family_nbr int, title_text citext, title_1 citext, title_2 citext, title_3 citext, title_4 citext, title_5 citext);" > /dev/null 2>&1
psql -U postgres -d snzts -c "COPY family (subject_code, family_code, family_nbr, title_text, title_1, title_2, title_3, title_4, title_5) FROM '/tmp/psqldata/family.csv' DELIMITER ',' CSV HEADER;" > /dev/null 2>&1

printf "series...\n"
psql -U postgres -d snzts -c "CREATE TABLE series (subject_code citext, family_code citext, family_nbr int, series_code citext, series_interval_nbr int, mnth_offset_nbr int, magnitude_nbr int, unit_text citext, code_1 citext, code_2 citext, code_3 citext, code_4 citext, code_5 citext, description_1 citext, description_2 citext, description_3 citext, description_4 citext, description_5 citext);" > /dev/null 2>&1
psql -U postgres -d snzts -c "COPY series (subject_code, family_code, family_nbr, series_code, series_interval_nbr, mnth_offset_nbr, magnitude_nbr, unit_text, code_1, code_2, code_3, code_4, code_5, description_1, description_2, description_3, description_4, description_5) FROM '/tmp/psqldata/series.csv' DELIMITER ',' CSV HEADER;" > /dev/null 2>&1

printf "data...\n"
psql -U postgres -d snzts -c "CREATE TABLE data (series_code citext, period varchar, value double precision, status citext, m int, n int);" > /dev/null 2>&1
psql -U postgres -d snzts -c "COPY data (series_code, period, value, status, m, n) FROM '/tmp/psqldata/data.csv' DELIMITER ',' CSV HEADER;" > /dev/null 2>&1

printf "creating view series_info (series + subject + family)...\n"
psql -U postgres -d snzts -c "CREATE MATERIALIZED VIEW series_info AS  
SELECT 
  a.subject_code, b.family_code, b.family_nbr, c.series_code, 
  a.title_text as subject_title,
  b.title_text as family_title,
  b.title_1, b.title_2, b.title_3, b.title_4, b.title_5,
  c.series_interval_nbr, c.mnth_offset_nbr, c.magnitude_nbr, c.unit_text, 
  c.code_1, c.code_2, c.code_3, c.code_4, c.code_5, 
  c.description_1, c.description_2, c.description_3, c.description_4, c.description_5
FROM
  subject a
INNER JOIN
  family b
ON
  a.subject_code = b.subject_code
INNER JOIN
  series c
ON
  a.subject_code = c.subject_code
  and b.family_code = c.family_code
  and b.family_nbr = c.family_nbr
order by
  a.subject_code,
  b.family_code, b.family_nbr,
  c.series_code
WITH NO DATA" > /dev/null 2>&1

printf "building indexes...\n"
psql -U postgres -d snzts -c "create unique index i1 on subject (subject_code)" > /dev/null 2>&1
psql -U postgres -d snzts -c "create unique index i2 on family (subject_code, family_code, family_nbr)" > /dev/null 2>&1
psql -U postgres -d snzts -c "create unique index i3 on series (subject_code, family_code, family_nbr, series_code)" > /dev/null 2>&1
psql -U postgres -d snzts -c "create index i4 on data (series_code)" > /dev/null 2>&1
psql -U postgres -d snzts -c "create unique index i5 on series_info (subject_code, family_code, family_nbr, series_code)"

printf "refreshing view series_info...\n"
psql -U postgres -d snzts -c "REFRESH MATERIALIZED VIEW series_info;" > /dev/null 2>&1

# setup permissions
psql -U postgres -d snzts -c 'grant usage on schema public to webuser;' > /dev/null 2>&1
psql -U postgres -d snzts -c 'grant select on all tables in schema public to webuser;' > /dev/null 2>&1

# remove files
rm -fR /tmp/psqldata/*.csv