package io.tcj.feeds;

import com.squareup.wire.Message;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import misk.web.Post;
import misk.web.RequestBody;
import misk.web.RequestContentType;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;

import javax.inject.Inject;
import java.util.List;

/**
 * this is how a feed producer exposes it's published entries to remote feed consumers.
 *
 *
 */
public class FeedEntryWebAction implements WebAction {
  @Inject FeedEntryFetcher<KvRecord, Kv> feedEntryFetcher;

  @Post(pathPattern = "/_api/feeds/fetch")
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  public FeedFetchResponse<Kv> fetch(@RequestBody FeedFetchRequest req) {
    return feedEntryFetcher.fetch(req);
  }

  public record FeedFetchRequest(
      String feed_name,
      String after,
      int limit,
      int shard,
      int num_shards
  ) {
  }
  public record FeedFetchResponse<Rec extends Message<?,?>>(List<Rec> entries, String next_after) {
  }
}
