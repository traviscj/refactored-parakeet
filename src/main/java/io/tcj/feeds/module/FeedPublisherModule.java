package io.tcj.feeds.module;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.squareup.wire.Message;
import io.tcj.feeds.FeedPublisher;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.impl.GenericDbFeedFetcher;
import io.tcj.feeds.model.FeedTableDef;
import io.tcj.feeds.model.PublishedFeedDef;
import org.jooq.UpdatableRecord;

/**
 * Per-feed PUBLISH wiring: stamps {@code feed_sync_id} onto one table's rows.
 *
 * <p>A {@link PrivateModule} so each feed gets its own {@link PublishedFeedDef} and
 * {@link FeedTableDef}. {@code PublishedFeedDef} is non-generic, so two feeds
 * binding it in one space would collide -- privacy is what makes "install once per
 * feed" safe. Exposes {@code FeedPublisher<Rec,Dto>} (for a trigger to call) and
 * {@code DbFeedFetcher<Rec,Dto>} (reused by the exposer and any co-located consumer).
 *
 * <p>TODO(trigger): {@link FeedPublisher#processBatch()} still needs a caller --
 * a WebAction (see {@link FeedPublisher.FeedPublishWebAction}) or a scheduled
 * RepeatedTaskQueue job. Left to the composing module for now.
 *
 * @param <Rec> the jOOQ record type of the published table.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public final class FeedPublisherModule<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>
        extends PrivateModule {
    private final Class<Rec> recordClass;
    private final Class<Dto> dtoClass;
    private final FeedTableDef<Rec, Dto> tableDef;
    private final PublishedFeedDef publishedFeedDef;

    public FeedPublisherModule(Class<Rec> recordClass, Class<Dto> dtoClass,
                               FeedTableDef<Rec, Dto> tableDef, PublishedFeedDef publishedFeedDef) {
        this.recordClass = recordClass;
        this.dtoClass = dtoClass;
        this.tableDef = tableDef;
        this.publishedFeedDef = publishedFeedDef;
    }

    @Override
    protected void configure() {
        Key<FeedTableDef<Rec, Dto>> tableDefKey = FeedKeys.parameterized(FeedTableDef.class, recordClass, dtoClass);
        Key<DbFeedFetcher<Rec, Dto>> fetcherKey = FeedKeys.parameterized(DbFeedFetcher.class, recordClass, dtoClass);
        Key<GenericDbFeedFetcher<Rec, Dto>> implKey = FeedKeys.parameterized(GenericDbFeedFetcher.class, recordClass, dtoClass);
        Key<FeedPublisher<Rec, Dto>> publisherKey = FeedKeys.parameterized(FeedPublisher.class, recordClass, dtoClass);

        bind(tableDefKey).toInstance(tableDef);
        bind(PublishedFeedDef.class).toInstance(publishedFeedDef);
        bind(fetcherKey).to(implKey);
        bind(publisherKey);

        expose(publisherKey);
        expose(fetcherKey);
    }
}
