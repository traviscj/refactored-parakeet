package io.tcj.feeds;

import io.tcj.feeds.FeedEntryWebAction.FeedFetchRequest;
import io.tcj.feeds.FeedEntryWebAction.FeedFetchResponse;

/**
 * an internal version of the web action, operating on actual DTO objects.
 */
public class FeedEntryFetcher<Rec extends FeedEntry<Rec>> {
  public FeedFetchResponse<Rec> fetch(FeedFetchRequest req) {
    return null;
  }
}
