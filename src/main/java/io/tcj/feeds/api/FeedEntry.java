package io.tcj.feeds.api;

import io.tcj.feeds.model.FeedCursor;

/**
 * envelope for a feed-published record, along with its associated {@link FeedCursor}.
 *
 * Rec: the contents of the envelope -- the record your app feed publishes.
 */
public interface FeedEntry<Rec> {
  FeedCursor feedCursor();
  Rec record();
}
