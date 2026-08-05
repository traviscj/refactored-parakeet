package io.tcj.feeds.impl;

import io.tcj.db.TraviscjDb;
import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.jooq.tables.records.FeedCursorsRecord;
import misk.jooq.JooqTransacter;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.inject.Inject;

import static io.tcj.jooq.tables.FeedCursors.FEED_CURSORS;

/**
 * {@link FeedCursorStore} backed by the dedicated {@code feed_cursors} table:
 * one row per (consumer, shard); unsharded consumers use shard=0, shard_count=1.
 * {@code token} stores the last-consumed {@code feed_sync_id} as a string;
 * {@code last_fetched_at} is bumped whenever the cursor advances.
 */
public class FeedCursorStoreImpl implements FeedCursorStore {
    @Inject
    @TraviscjDb
    JooqTransacter jooqTransacter;

    // Explicit @Inject constructor so the class is constructible under strict
    // injectors; field injection still supplies jooqTransacter.
    @Inject
    public FeedCursorStoreImpl() {}

    @Override
    public FeedCursor get(String feedName, int shard) {
        return jooqTransacter.transaction(session -> {
            DSLContext ctx = session.getCtx();
            FeedCursorsRecord record = fetch(ctx, feedName, shard);
            if (record == null) {
                // Create-on-first-use: token '0', enabled, unsharded defaults.
                insertIgnore(ctx, feedName, shard, "0");
                record = fetch(ctx, feedName, shard);
            }
            long token = record.getToken() == null ? 0L : Long.parseLong(record.getToken());
            return new FeedCursor(feedName, token, record.getShard(), record.getShardCount());
        });
    }

    @Override
    public void put(String feedName, int shard, String cursor) {
        jooqTransacter.transaction(session -> {
            DSLContext ctx = session.getCtx();
            int updated = ctx.update(FEED_CURSORS)
                    .set(FEED_CURSORS.TOKEN, cursor)
                    .set(FEED_CURSORS.LAST_FETCHED_AT, DSL.currentLocalDateTime())
                    .where(FEED_CURSORS.CONSUMER.eq(feedName).and(FEED_CURSORS.SHARD.eq(shard)))
                    .execute();
            if (updated == 0) {
                insertIgnore(ctx, feedName, shard, cursor);
            }
            return null;
        });
    }

    private static FeedCursorsRecord fetch(DSLContext ctx, String feedName, int shard) {
        return ctx.selectFrom(FEED_CURSORS)
                .where(FEED_CURSORS.CONSUMER.eq(feedName).and(FEED_CURSORS.SHARD.eq(shard)))
                .fetchOne();
    }

    private static void insertIgnore(DSLContext ctx, String feedName, int shard, String token) {
        ctx.insertInto(FEED_CURSORS)
                .set(FEED_CURSORS.CONSUMER, feedName)
                .set(FEED_CURSORS.SHARD, shard)
                .set(FEED_CURSORS.SHARD_COUNT, 1)
                .set(FEED_CURSORS.TOKEN, token)
                .set(FEED_CURSORS.ENABLED, (byte) 1)
                .onDuplicateKeyIgnore()
                .execute();
    }
}
