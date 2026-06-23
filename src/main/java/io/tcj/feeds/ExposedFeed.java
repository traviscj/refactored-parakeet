package io.tcj.feeds;

import io.tcj.protos.feeds.FeedFetchRequest;
import io.tcj.protos.feeds.FeedFetchResponse;

/**
 * Type-erased view of one published feed for the generic
 * {@code FeedSourceService.Fetch} endpoint: takes a wire request and returns a wire
 * response of serialized DTO bytes, hiding the feed's {@code Rec}/{@code Dto} types.
 *
 * <p>Feeds register an instance keyed by name into a {@code Map<String, ExposedFeed>}
 * (a Guice MapBinder), so a single Fetch action can serve every feed without
 * referencing any concrete DTO. See {@code io.tcj.feeds.impl.DbExposedFeed} for the
 * DB-backed implementation and {@code io.tcj.feeds.FeedSourceFetchRpc} for the router.
 */
public interface ExposedFeed {
    FeedFetchResponse fetch(FeedFetchRequest request);

    /** @return the jOOQ record type backing this feed (for topology introspection). */
    Class<?> recordType();
}
