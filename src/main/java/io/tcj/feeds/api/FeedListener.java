package io.tcj.feeds.api;

import java.util.List;

/**
 * Application hook invoked by a {@code FeedConsumer} with each batch of entries it
 * pulls from a feed source. Implementations react to entries (e.g. enqueue a
 * notification); the consumer owns cursor bookkeeping.
 *
 * @param <Dto> the wire message type carried by the feed.
 */
public interface FeedListener<Dto> {
  /**
   * Processes one batch of feed entries.
   *
   * @param entries entries fetched since the consumer's last cursor position.
   */
  void process(List<Dto> entries);
}
