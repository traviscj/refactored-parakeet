package io.tcj.feeds.impl;

import com.squareup.wire.Message;
import io.tcj.db.TraviscjDb;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.api.FeedEntry;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.feeds.model.FeedTableDef;
import io.tcj.feeds.model.FetchResult;
import jakarta.inject.Singleton;
import misk.jooq.JooqTransacter;
import org.jooq.Condition;
import org.jooq.Result;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Table-agnostic {@link DbFeedFetcher}: drives any feed-published table from the
 * column/mapping handles in its injected {@link FeedTableDef}. Deliberately
 * references only {@code org.jooq.*} (never the generated jOOQ package), so the
 * same engine serves every feed and ports to other tables/codebases unchanged.
 *
 * @param <Rec> the jOOQ record type backing this feed.
 * @param <Dto> the wire message type emitted to consumers.
 */
@Singleton
public class GenericDbFeedFetcher<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>
        implements DbFeedFetcher<Rec, Dto> {
    private static final Logger log = LoggerFactory.getLogger(GenericDbFeedFetcher.class);

    @Inject @TraviscjDb JooqTransacter jooqTransacter;
    @Inject FeedTableDef<Rec, Dto> def;

    @Override
    @SuppressWarnings("unchecked") // table().getRecordType() is Class<? extends Rec>; it IS this feed's Rec
    public Class<Rec> recordType() {
        return (Class<Rec>) def.table().getRecordType();
    }

    @Override
    public int publish(List<? extends FeedEntry<Rec>> feedEntries, int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            List<Rec> records = feedEntries.stream()
                    .limit(limit)
                    .map(FeedEntry::record)
                    .collect(Collectors.toList());
            records.forEach(record -> {
                log.info("Publishing feed entry: id={} feed_sync_id={}",
                        record.get(def.id()), record.get(def.feedSyncId()));
                jooqSession.getCtx().update(def.table())
                        .set(def.feedSyncId(), record.get(def.feedSyncId()))
                        .where(idEquals(def.id(), record))
                        .execute();
            });
            return records.size();
        });
    }

    @Override
    public List<? extends FeedEntry<Rec>> fetchUnpublished(int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            Result<Rec> records = jooqSession.getCtx()
                    .selectFrom(def.table())
                    .where(def.feedSyncId().isNull())
                    .limit(limit)
                    .fetch();
            return records.stream()
                    .map(record -> new GenericFeedEntry<>(record, def.feedSyncId()))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public FetchResult<Dto> fetchPublished(FeedCursor feedCursor, int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            Condition condition = def.feedSyncId().greaterThan(feedCursor.feedSyncId());
            if (def.shard() != null && feedCursor.numShards() != -1) {
                condition = condition.and(def.shard().mod(feedCursor.numShards()).eq(feedCursor.shard()));
            }
            Result<Rec> records = jooqSession.getCtx()
                    .selectFrom(def.table())
                    .where(condition)
                    .limit(limit)
                    .fetch();
            Long next = records.stream()
                    .map(record -> record.get(def.feedSyncId()))
                    .max(Long::compareTo)
                    .orElse(0L);
            List<Dto> dtos = records.stream().map(def.toDto()).collect(Collectors.toList());
            return new FetchResult<>(dtos, next.toString());
        });
    }

    // captures the id column's value type once, sidestepping the wildcard on FeedTableDef.id()
    private static <R extends UpdatableRecord<R>, T> Condition idEquals(TableField<R, T> idField, R record) {
        return idField.eq(record.get(idField));
    }
}
