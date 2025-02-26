package io.tcj.feeds.api;

import java.util.List;

public interface FeedListener<T extends FeedEntry<T>> {
  void process(List<T> entries);
}
