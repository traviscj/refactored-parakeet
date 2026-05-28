package io.tcj.feeds;

import com.squareup.wire.Message;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.feeds.model.FetchRequest;
import io.tcj.feeds.model.FetchResult;
import jakarta.inject.Inject;
import org.jooq.UpdatableRecord;

/**
 * In-process typed fetch: turns a {@link FetchRequest} into a {@link FeedCursor} and
 * reads the next batch of DTOs from the local {@link DbFeedFetcher}.
 *
 * <p>This is the co-located path used by a {@code FeedConsumer} in the same service
 * as the producer. The over-the-wire equivalent is the generic
 * {@code FeedSourceService.Fetch} endpoint (see {@link io.tcj.feeds.FeedSourceFetchRpc}
 * and {@link io.tcj.feeds.ExposedFeed}).
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public class FeedEntryFetcher<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>> {
  @Inject
  DbFeedFetcher<Rec, Dto> dbFeedFetcher;

  public FetchResult<Dto> fetch(FetchRequest req) {
    return dbFeedFetcher.fetchPublished(
            new FeedCursor(req.feedName(), Long.parseLong(req.after()), req.shard(), req.numShards()),
            req.limit()
    );
  }
}
