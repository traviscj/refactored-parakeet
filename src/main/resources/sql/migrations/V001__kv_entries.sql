CREATE TABLE kv (
    id serial,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    version int not null default 0,
    feed_sync_id bigint default null,
    shard int default 0,

    ns varchar(255),
    k varchar(255),
    v varchar(4096),
    constraint u_ns_k UNIQUE (ns, k)
)
