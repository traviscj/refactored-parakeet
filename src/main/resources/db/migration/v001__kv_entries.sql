CREATE TABLE `kv` (
    `id` bigint(22) NOT NULL AUTO_INCREMENT,
    `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `feed_sync_id` bigint(22) DEFAULT NULL,
    `shard` int(11) NOT NULL DEFAULT '0',
    `ns` varchar(255) NOT NULL DEFAULT '-',
    `k` varchar(255) NOT NULL,
    `v` longblob NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `u_ns_k` (`ns`,`k`),
    UNIQUE KEY `u_fsi` (`feed_sync_id`),
    KEY `idx_ca` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
