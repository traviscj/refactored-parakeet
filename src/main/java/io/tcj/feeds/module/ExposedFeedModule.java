package io.tcj.feeds.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;
import com.squareup.wire.Message;
import io.tcj.feeds.ExposedFeed;
import io.tcj.feeds.FeedEntryFetcher;
import io.tcj.feeds.impl.DbExposedFeed;
import org.jooq.UpdatableRecord;

/**
 * Per-feed EXPOSE wiring: registers one published feed in the {@code FeedSourceService}
 * registry (keyed by {@code feedName}) so the single generic
 * {@code /io.tcj.FeedSourceService/Fetch} endpoint can serve it, and binds the
 * in-process {@code FeedEntryFetcher<Rec,Dto>} for any co-located consumer.
 *
 * <p>Over the wire the feed is exposed as serialized DTO bytes via {@link DbExposedFeed};
 * {@link FeedSourceModule} installs the routing endpoint. The {@link Exposure} hint
 * records the intended consumer format ({@code WIRE_PROTO} is the wired path today).
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public final class ExposedFeedModule<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>
        extends AbstractModule {

    /** The wire format a published feed is exposed in. */
    public enum Exposure {JSON_HTTP, WIRE_PROTO, IN_PROCESS}

    private final String feedName;
    private final Class<Rec> recordClass;
    private final Class<Dto> dtoClass;
    private final Exposure exposure;

    public ExposedFeedModule(String feedName, Class<Rec> recordClass, Class<Dto> dtoClass, Exposure exposure) {
        this.feedName = feedName;
        this.recordClass = recordClass;
        this.dtoClass = dtoClass;
        this.exposure = exposure;
    }

    /** @return the format this feed is exposed in (informational hint for now). */
    public Exposure exposure() {
        return exposure;
    }

    @Override
    protected void configure() {
        Key<FeedEntryFetcher<Rec, Dto>> fetcherKey = FeedKeys.parameterized(FeedEntryFetcher.class, recordClass, dtoClass);
        bind(fetcherKey); // in-process typed fetch (used by a co-located FeedConsumer)

        // register this feed in the FeedSourceService routing table, keyed by name
        Key<DbExposedFeed<Rec, Dto>> exposedKey = FeedKeys.parameterized(DbExposedFeed.class, recordClass, dtoClass);
        bind(exposedKey);
        MapBinder.newMapBinder(binder(), String.class, ExposedFeed.class)
                .addBinding(feedName).to(exposedKey);
    }
}
