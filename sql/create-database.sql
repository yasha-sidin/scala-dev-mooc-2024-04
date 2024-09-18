--setup database
drop DATABASE IF EXISTS demo;
create DATABASE demo;
\c demo;

drop table if exists public.product;

create table if not exists public.product (
    id BIGINT NOT NULL,
    title TEXT,
    write_side_offset BIGINT NOT NULL,
    PRIMARY KEY(id)
);