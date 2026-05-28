package io.tcj.feeds.module;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.squareup.wire.Message;
import io.tcj.feeds.FeedConsumer;
import io.tcj.feeds.FeedEntryFetcher;
import io.tcj.feeds.api.FeedListener;
import io.tcj.feeds.model.FeedConsumerDef;
import org.jooq.UpdatableRecord;

/**
 * Per-consumer CONSUME wiring: binds one {@link FeedConsumer} that pulls an exposed
 * feed and hands batches to a {@link FeedListener}.
 *
 * <p>A {@link PrivateModule} so each consumer gets its own {@link FeedConsumerDef}
 * (non-generic, would otherwise collide across consumers). Exposes
 * {@code FeedConsumer<Rec,Dto>}.
 *
 * <p>TODO(shards + schedule): a real consumer fans out over {@code numShards}:
 * <ul>
 *   <li>persist N {@code feed_cursors} (one per shard) -- needs a feed_cursors table/migration;</li>
 *   <li>register N scheduled jobs (e.g. misk {@code RepeatedTaskQueue}), job {@code i} polling rows
 *       where {@code shard % numShards == i}, each advancing its own cursor.</li>
 * </ul>
 * This skeleton wires a single {@link FeedConsumer} (per the supplied
 * {@link FeedConsumerDef}); the N-shard cursor creation + scheduling is not yet wired.
 *
 * <p>TODO(remote): the bound {@link FeedEntryFetcher} reads the <em>local</em>
 * exposed feed (same process/DB). A consumer of a remote feed needs an HTTP-backed
 * fetcher variant instead.
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is consumed as.
 */
public final class FeedConsumerModule<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>
        extends PrivateModule {
    private final Class<Rec> recordClass;
    private final Class<Dto> dtoClass;
    private final FeedConsumerDef consumerDef;
    private final Class<? extends FeedListener<Dto>> listenerClass;

    public FeedConsumerModule(Class<Rec> recordClass, Class<Dto> dtoClass,
                              FeedConsumerDef consumerDef,
                              Class<? extends FeedListener<Dto>> listenerClass) {
        this.recordClass = recordClass;
        this.dtoClass = dtoClass;
        this.consumerDef = consumerDef;
        this.listenerClass = listenerClass;
    }

    @Override
    protected void configure() {
        Key<FeedListener<Dto>> listenerKey = FeedKeys.parameterized(FeedListener.class, dtoClass);
        Key<FeedEntryFetcher<Rec, Dto>> fetcherKey = FeedKeys.parameterized(FeedEntryFetcher.class, recordClass, dtoClass);
        Key<FeedConsumer<Rec, Dto>> consumerKey = FeedKeys.parameterized(FeedConsumer.class, recordClass, dtoClass);

        bind(FeedConsumerDef.class).toInstance(consumerDef);
        bind(listenerKey).to(listenerClass);
        bind(fetcherKey); // local in-process fetch; see TODO(remote)
        bind(consumerKey);

        expose(consumerKey);
        // TODO(shards + schedule): see class javadoc.
    }
}
