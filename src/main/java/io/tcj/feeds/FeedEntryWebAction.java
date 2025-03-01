package io.tcj.feeds;

import com.squareup.wire.Message;
import io.tcj.protos.kv.Kv;
import misk.web.Post;
import misk.web.RequestBody;
import misk.web.RequestContentType;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this is how a feed producer exposes it's published entries to remote feed consumers.
 *
 *
 */
public class FeedEntryWebAction implements WebAction {
  @Inject FeedEntryFetcher feedEntryFetcher;

  @Post(pathPattern = "/_api/feeds/fetch")
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  public io.tcj.protos.feeds.FeedFetchResponse fetch(@RequestBody FeedFetchRequest req) {
    FeedFetchResponse<Kv> fetch = feedEntryFetcher.fetch(req);
      return new io.tcj.protos.feeds.FeedFetchResponse.Builder()
              .next_after(fetch.next_after)
              .entries(fetch.entries().stream().map(Kv::encodeByteString).collect(Collectors.toList()))
              .build();
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
