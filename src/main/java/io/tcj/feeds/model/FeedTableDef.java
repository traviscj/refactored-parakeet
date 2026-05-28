package io.tcj.feeds.model;

import com.squareup.wire.Message;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;

import java.util.function.Function;

/**
 * Binds one jOOQ table to the generic feed engine. This is the single place a
 * concrete feed names its table, the columns the engine needs, and how to turn a
 * row into a wire DTO -- everything table-specific that
 * {@code GenericDbFeedFetcher} would otherwise have to hard-code.
 *
 * <p>Bind one instance per feed (typically {@code toInstance(...)} in a Guice
 * module). The target table only has to expose a {@code feed_sync_id} column (and
 * optionally a {@code shard} column); the engine itself imports nothing from the
 * generated jOOQ package, so it ports unchanged to other tables/codebases.
 *
 * @param <Rec>       the jOOQ record type backing this feed.
 * @param <Dto>       the wire message type emitted to consumers.
 * @param table       the feed-published table.
 * @param feedSyncId  the {@code feed_sync_id} column (null until published).
 * @param id          the primary key column, used to target the publish update.
 * @param shard       the shard column for sharded fan-out, or {@code null} to disable sharding.
 * @param toDto       maps a row to the DTO sent to consumers.
 */
public record FeedTableDef<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>(
        Table<Rec> table,
        TableField<Rec, Long> feedSyncId,
        TableField<Rec, ?> id,
        TableField<Rec, Integer> shard,
        Function<Rec, Dto> toDto
) {
}
