package io.tcj.feeds.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.squareup.wire.Message;
import io.tcj.feeds.FeedEntryFetcher;
import org.jooq.UpdatableRecord;

/**
 * Per-feed EXPOSE wiring: makes one published feed consumable in a chosen format.
 *
 * <p>Binds {@code FeedEntryFetcher<Rec,Dto>}, which reads published rows via the
 * {@code DbFeedFetcher<Rec,Dto>} exposed by {@link FeedPublisherModule}. The
 * transport itself is a concrete endpoint that Misk routes by class, so it cannot
 * be installed generically here -- the composing module installs the endpoint
 * matching {@link #exposure()}:
 * <ul>
 *   <li>{@link Exposure#JSON_HTTP}: a WebAction (moshi/JSON), e.g. {@code FeedEntryWebAction}.</li>
 *   <li>{@link Exposure#WIRE_PROTO}: a {@code FeedSourceService.Fetch} gRPC impl (see feeds.proto).</li>
 *   <li>{@link Exposure#IN_PROCESS}: no endpoint; a co-located consumer injects the fetcher directly.</li>
 * </ul>
 * The same published feed may be exposed several ways at once (different consumers
 * want different formats); install one {@code ExposedFeedModule} per format.
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public final class ExposedFeedModule<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>>
        extends AbstractModule {

    /** The wire format a published feed is exposed in. */
    public enum Exposure {JSON_HTTP, WIRE_PROTO, IN_PROCESS}

    private final Class<Rec> recordClass;
    private final Class<Dto> dtoClass;
    private final Exposure exposure;

    public ExposedFeedModule(Class<Rec> recordClass, Class<Dto> dtoClass, Exposure exposure) {
        this.recordClass = recordClass;
        this.dtoClass = dtoClass;
        this.exposure = exposure;
    }

    /** @return the format this feed is exposed in; the composing module installs the matching endpoint. */
    public Exposure exposure() {
        return exposure;
    }

    @Override
    protected void configure() {
        Key<FeedEntryFetcher<Rec, Dto>> fetcherKey = FeedKeys.parameterized(FeedEntryFetcher.class, recordClass, dtoClass);
        bind(fetcherKey);
        // TODO(endpoint): install the concrete endpoint for `exposure` (web action / FeedSourceService impl).
    }
}
