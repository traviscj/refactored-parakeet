package io.tcj.feeds;

import com.squareup.wire.Message;
import io.tcj.feeds.FeedEntryWebAction.FeedFetchRequest;
import io.tcj.feeds.FeedEntryWebAction.FeedFetchResponse;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.model.FeedCursor;
import jakarta.inject.Inject;
import org.jooq.UpdatableRecord;

/**
 * an internal version of the web action, operating on actual DTO objects.
 *
 * (not totally convinced we need this anymore?)
 *
 * ok, this _is_ required, and we shouldn't use Rec extends UpdatableRecord&lt;Rec&gt;.
 *
 * Rather, this _should_ be a DTO - a data transfer object, like a proto!
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public class FeedEntryFetcher<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>> {
  @Inject
  DbFeedFetcher<Rec, Dto> dbFeedFetcher;

  public FeedFetchResponse<Dto> fetch(FeedFetchRequest req) {
    return dbFeedFetcher.fetchPublished(
            new FeedCursor(req.feed_name(), Long.parseLong(req.after()), req.shard(), req.num_shards()),
            req.limit()
    );
  }
}
