create table vacancies
(
    id           uuid primary key,
    source       varchar(30)   not null,
    external_id  varchar(255)  not null,
    title        varchar(255)  not null,
    company_name varchar(255)  not null,
    location     varchar(255),
    remote       boolean       not null,
    url          varchar(1000) not null,
    description  text          not null,
    posted_at    timestamp,
    ingested_at  timestamp     not null,
    status       varchar(20)   not null,
    created_at   timestamp     not null,
    updated_at   timestamp     not null
);

alter table vacancies
    add constraint uk_vacancies_source_external_id
        unique (source, external_id);
