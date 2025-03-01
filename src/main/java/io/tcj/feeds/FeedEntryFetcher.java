package io.tcj.feeds;

import io.tcj.feeds.FeedEntryWebAction.FeedFetchRequest;
import io.tcj.feeds.FeedEntryWebAction.FeedFetchResponse;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import jakarta.inject.Inject;

/**
 * an internal version of the web action, operating on actual DTO objects.
 *
 * (not totally convinced we need this anymore?)
 *
 * ok, this _is_ required, and we shouldn't use Rec extends UpdatableRecord&lt;Rec&gt;.
 *
 * Rather, this _should_ be a DTO - a data transfer object, like a proto!
 */
public class FeedEntryFetcher {
  @Inject
  DbFeedFetcher<KvRecord, Kv> dbFeedFetcher;

  public FeedFetchResponse<Kv> fetch(FeedFetchRequest req) {

    return dbFeedFetcher.fetchPublished(
            new FeedCursor(req.feed_name(), Long.parseLong(req.after()), req.shard(), req.num_shards()),
            req.limit()
    );
  }
}
