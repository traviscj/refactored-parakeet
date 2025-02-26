package io.tcj.feeds;

import java.util.List;
import javax.inject.Inject;

import io.tcj.feeds.api.FeedEntry;
import misk.web.Post;
import retrofit2.http.Body;

/**
 * this is how a feed producer exposes it's published entries to remote feed consumers.
 *
 *
 */
public class FeedEntryWebAction {
  @Inject
  FeedEntryFetcher feedEntryFetcher;
  @Post(pathPattern = "/_api/feeds/fetch")
  public FeedFetchResponse fetch(@Body FeedFetchRequest req) {
    throw new RuntimeException("has not been implemented yet");
  }

  public record FeedFetchRequest(
      String feed_name,
      String after,
      int limit,
      int shard
  ) {
  }
  public record FeedFetchResponse<T extends FeedEntry<T>>(List<T> entries, String next_after) {
//    public ;
//    public String next_after;
  }
}
