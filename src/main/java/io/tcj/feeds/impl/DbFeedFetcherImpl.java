package io.tcj.feeds.impl;

import io.tcj.db.TraviscjDb;
import io.tcj.feeds.FeedEntryWebAction;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.api.FeedEntry;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import jakarta.inject.Singleton;
import misk.jooq.JooqTransacter;
import org.jooq.Condition;
import org.jooq.Result;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.tcj.jooq.tables.Kv.KV;

@Singleton
public class DbFeedFetcherImpl implements DbFeedFetcher<KvRecord,Kv> {
    private static final Logger log = LoggerFactory.getLogger(DbFeedFetcherImpl.class);
    @Inject @TraviscjDb JooqTransacter jooqTransacter;

    @Override
    public Class<KvRecord> recordType() {
        return KvRecord.class;
    }

    @Override
    public int publish(List<? extends FeedEntry<KvRecord>> feedEntries, int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            List<? extends UpdatableRecord<KvRecord>> collect = feedEntries.stream()
                    .limit(limit)
                    .map(FeedEntry::record)
                    .collect(Collectors.toList());

            // this worked!
            collect.forEach(record -> {
                log.info(String.format("Publishing feed: kv.id=%s kv.fsi=%s", record.get("id"), record.get("feed_sync_id")));
                jooqSession.getCtx().update(KV)
                        .set(KV.FEED_SYNC_ID, record.get(KV.FEED_SYNC_ID))
                        .where(KV.ID.eq(record.get(KV.ID)))
                        .execute();
            });
            return collect.size();

            // this didn't!
//            Batch batch = jooqSession.getCtx().batchUpdate(collect);
//            batch.execute();
//            jooqSession.getCtx().commit();
//            return batch.size();
        });
    }

    @Override
    public List<? extends FeedEntry<KvRecord>> fetchUnpublished(int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            Result<KvRecord> fetch = jooqSession.getCtx()
                    .selectFrom(KV)
                    .where(KV.FEED_SYNC_ID.isNull())
                    .limit(limit)
                    .fetch();
            return fetch.stream()
                    .map(KvFeedEntry::new)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public FeedEntryWebAction.FeedFetchResponse<Kv> fetchPublished(FeedCursor feedCursor, int limit) {
        return jooqTransacter.transaction(jooqSession -> {
            Condition ge = KV.FEED_SYNC_ID.greaterThan(feedCursor.feedSyncId());
            if (feedCursor.numShards() != -1) {
                ge = ge
                        .and(KV.SHARD.mod(feedCursor.numShards()).eq(feedCursor.shard()));
            }
            Result<KvRecord> kvRecords = jooqSession.getCtx()
                    .selectFrom(KV)
                    .where(ge)
                    .limit(limit)
                    .fetch();
            Long next = kvRecords.stream().map(KvRecord::getFeedSyncId).max(Long::compareTo).orElse(0L);
            List<Kv> kvList = kvRecords.stream().map(kvRecord -> new Kv.Builder()
                            .ns(kvRecord.getNs())
                            .k(kvRecord.getK())
                            .v(kvRecord.getV())
                            .build())
                    .toList();
            return new FeedEntryWebAction.FeedFetchResponse<Kv>(kvList, next.toString());
        });
    }
}
